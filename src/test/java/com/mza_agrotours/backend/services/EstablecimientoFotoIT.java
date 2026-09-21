package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.establecimiento.DTOUpdEstablecimientoRequest;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.repositories.ArchivoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class EstablecimientoFotoIT extends AbstractIntegrationTest {

    @Autowired
    private EstablecimientoService establecimientoService;

    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private ArchivoService archivoService;

    @Autowired
    private ArchivoRepository archivoRepository;

    @Autowired
    private FixtureEstablecimiento fixtureEstablecimiento;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void guardaLaPortadaYLaDevuelveConUrlDeDescarga() {
        Establecimiento establecimiento = this.fixtureEstablecimiento.establecimientoActivo();
        String key = subirFoto();

        this.establecimientoService.modificarEstablecimiento(
                establecimiento.getId(), pedido(establecimiento, new ArchivoClaimRequest(key, "portada.jpg")));

        assertThat(this.establecimientoService.obtenerDatosEstablecimiento(establecimiento.getId()).getFoto())
                .satisfies(foto -> {
                    assertThat(foto.getKey()).isEqualTo(key);
                    assertThat(foto.getNombre()).isEqualTo("portada.jpg");
                    assertThat(foto.getExtension()).isEqualTo("jpg");
                    assertThat(foto.getDownloadUrl()).contains(key);
                });
    }

    /**
     * Guardar el formulario sin tocar la portada no puede fallar: reclamar de
     * nuevo una key ya asociada la rechazaria por duplicada.
     */
    @Test
    void reenviarLaMismaKeyNoRompe() {
        Establecimiento establecimiento = this.fixtureEstablecimiento.establecimientoActivo();
        String key = subirFoto();
        ArchivoClaimRequest misma = new ArchivoClaimRequest(key, "portada.jpg");

        this.establecimientoService.modificarEstablecimiento(establecimiento.getId(), pedido(establecimiento, misma));

        assertThatCode(() -> this.establecimientoService
                .modificarEstablecimiento(establecimiento.getId(), pedido(establecimiento, misma)))
                .doesNotThrowAnyException();

        assertThat(this.establecimientoService.obtenerDatosEstablecimiento(establecimiento.getId())
                .getFoto().getKey()).isEqualTo(key);
    }

    @Test
    void reemplazarLaPortadaBorraElArchivoAnterior() {
        Establecimiento establecimiento = this.fixtureEstablecimiento.establecimientoActivo();
        String vieja = subirFoto();
        String nueva = subirFoto();

        this.establecimientoService.modificarEstablecimiento(
                establecimiento.getId(), pedido(establecimiento, new ArchivoClaimRequest(vieja, "vieja.jpg")));
        this.establecimientoService.modificarEstablecimiento(
                establecimiento.getId(), pedido(establecimiento, new ArchivoClaimRequest(nueva, "nueva.jpg")));

        assertThat(this.establecimientoService.obtenerDatosEstablecimiento(establecimiento.getId())
                .getFoto().getKey()).isEqualTo(nueva);
        assertThat(this.archivoRepository.existsByKey(vieja)).isFalse();
    }

    @Test
    void mandarNullQuitaLaPortada() {
        Establecimiento establecimiento = this.fixtureEstablecimiento.establecimientoActivo();
        String key = subirFoto();

        this.establecimientoService.modificarEstablecimiento(
                establecimiento.getId(), pedido(establecimiento, new ArchivoClaimRequest(key, "portada.jpg")));
        this.establecimientoService.modificarEstablecimiento(establecimiento.getId(), pedido(establecimiento, null));

        assertThat(this.establecimientoService.obtenerDatosEstablecimiento(establecimiento.getId()).getFoto()).isNull();
        assertThat(this.archivoRepository.existsByKey(key)).isFalse();
    }

    @Test
    void rechazaUnaKeyDeOtraCarpeta() {
        Establecimiento establecimiento = this.fixtureEstablecimiento.establecimientoActivo();
        String keyDeActividades = CarpetaArchivo.ACTIVIDADES.getPrefijo() + "/" + UUID.randomUUID() + ".jpg";

        assertThatThrownBy(() -> this.establecimientoService.modificarEstablecimiento(
                establecimiento.getId(), pedido(establecimiento, new ArchivoClaimRequest(keyDeActividades, "x.jpg"))))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("no es una key de establecimientos");
    }

    private DTOUpdEstablecimientoRequest pedido(Establecimiento establecimiento, ArchivoClaimRequest foto) {
        DTOUpdEstablecimientoRequest dto = new DTOUpdEstablecimientoRequest();
        dto.setNombre(establecimiento.getNombre());
        dto.setDescripcion(establecimiento.getDescripcion());
        dto.setTelefono(establecimiento.getTelefono());
        dto.setEmail(establecimiento.getEmail());
        dto.setCvu(establecimiento.getCvu());
        dto.setFoto(foto);
        return dto;
    }

    private String subirFoto() {
        ArchivoUploadRequest request = new ArchivoUploadRequest();
        request.setFilename("portada.jpg");
        request.setFileSize(1024);

        ArchivoUploadResponse firmado = this.archivoService.getSignedArchivo(request, CarpetaArchivo.ESTABLECIMIENTOS);
        try {
            int status = this.httpClient.send(
                    HttpRequest.newBuilder(URI.create(firmado.getUploadUrl()))
                            .header("Content-Type", firmado.getContentType())
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(new byte[1024]))
                            .build(),
                    HttpResponse.BodyHandlers.discarding()).statusCode();
            assertThat(status).isEqualTo(200);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return firmado.getKey();
    }
}
