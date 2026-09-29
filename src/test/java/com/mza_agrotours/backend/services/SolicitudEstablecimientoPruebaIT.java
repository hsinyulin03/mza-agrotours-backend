package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoPruebaUrlDTO;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimiento;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.SolicitudEstablecimientoError;
import com.mza_agrotours.backend.repositories.SolicitudEstablecimientoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureCatalogo;
import com.mza_agrotours.backend.support.FixtureUsuario;
import com.mza_agrotours.backend.support.Seq;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class SolicitudEstablecimientoPruebaIT extends AbstractIntegrationTest {

    @Autowired
    private SolicitudEstablecimientoService solicitudEstablecimientoService;

    @Autowired
    private SolicitudEstablecimientoRepository solicitudEstablecimientoRepository;

    @Autowired
    private ArchivoService archivoService;

    @Autowired
    private FixtureUsuario fixtureUsuario;

    @Autowired
    private FixtureCatalogo catalogo;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void devuelveUnaUrlFirmadaQueDescargaLaPrueba() {
        SolicitudEstablecimiento solicitud = solicitudConPrueba();
        Archivo prueba = solicitud.getPruebas().get(0);

        SolicitudEstablecimientoPruebaUrlDTO url = this.solicitudEstablecimientoService
                .obtenerUrlDePrueba(solicitud.getId().toString(), prueba.getId().toString());

        assertThat(url.getNombre()).isEqualTo("prueba.pdf");
        assertThat(url.getUrl()).contains("X-Amz-Signature");
        assertThat(descargar(url.getUrl())).isEqualTo(200);
    }

    /**
     * La carpeta es privada, asi que la misma url sin la firma no sirve: es lo
     * que distingue a las pruebas de las fotos de cultivos o actividades.
     */
    @Test
    void sinLaFirmaElBucketNoEntregaLaPrueba() {
        SolicitudEstablecimiento solicitud = solicitudConPrueba();
        Archivo prueba = solicitud.getPruebas().get(0);

        String url = this.solicitudEstablecimientoService
                .obtenerUrlDePrueba(solicitud.getId().toString(), prueba.getId().toString())
                .getUrl();

        assertThat(descargar(url.substring(0, url.indexOf('?')))).isEqualTo(403);
    }

    @Test
    void noFirmaLaPruebaDeOtraSolicitud() {
        SolicitudEstablecimiento propia = solicitudConPrueba();
        SolicitudEstablecimiento ajena = solicitudConPrueba();
        String idAjeno = ajena.getPruebas().get(0).getId().toString();

        assertThatThrownBy(() -> this.solicitudEstablecimientoService
                .obtenerUrlDePrueba(propia.getId().toString(), idAjeno))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(SolicitudEstablecimientoError.PRUEBA_NOT_FOUND);
    }

    @Test
    void rechazaUnaSolicitudInexistente() {
        assertThatThrownBy(() -> this.solicitudEstablecimientoService
                .obtenerUrlDePrueba(UUID.randomUUID().toString(), UUID.randomUUID().toString()))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(SolicitudEstablecimientoError.NOT_FOUND);
    }

    private SolicitudEstablecimiento solicitudConPrueba() {
        int n = Seq.next();

        SolicitudEstablecimiento solicitud = new SolicitudEstablecimiento();
        solicitud.setNombreEstablecimiento("Finca " + n);
        solicitud.setFechaHoraAlta(LocalDateTime.now());
        solicitud.setRazonSocial("Finca " + n + " S.A.");
        solicitud.setCuit(String.format("20%09d", n));
        solicitud.setDescripcionEstablecimiento("Solicitud de prueba " + n);
        solicitud.setDomicilioLegal("Calle Falsa " + n);
        solicitud.setEmail("finca" + n + "@test.local");
        solicitud.setTelefono("2610000000");
        solicitud.setCvu(String.format("%022d", n));
        solicitud.setDepartamento(catalogo.unDepartamento());
        solicitud.setUsuario(fixtureUsuario.usuario("solicitante"));
        solicitud.setPruebas(new ArrayList<>(List.of(subirPrueba())));

        return this.solicitudEstablecimientoRepository.save(solicitud);
    }

    private Archivo subirPrueba() {
        ArchivoUploadRequest request = new ArchivoUploadRequest();
        request.setFilename("prueba.pdf");
        request.setFileSize(1024);

        ArchivoUploadResponse firmado = this.archivoService
                .getSignedArchivo(request, CarpetaArchivo.SOLICITUDES_ESTABLECIMIENTO);
        try {
            int status = this.httpClient.send(
                    HttpRequest.newBuilder(URI.create(firmado.getUploadUrl()))
                            .header("Content-Type", firmado.getContentType())
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(
                                    "una prueba reservada".getBytes(StandardCharsets.UTF_8)))
                            .build(),
                    HttpResponse.BodyHandlers.discarding()).statusCode();
            assertThat(status).isEqualTo(200);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        return this.archivoService.reclamarArchivo(
                new ArchivoClaimRequest(firmado.getKey(), "prueba.pdf"),
                CarpetaArchivo.SOLICITUDES_ESTABLECIMIENTO);
    }

    private int descargar(String url) {
        try {
            return this.httpClient.send(
                    HttpRequest.newBuilder(URI.create(url)).GET().build(),
                    HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
