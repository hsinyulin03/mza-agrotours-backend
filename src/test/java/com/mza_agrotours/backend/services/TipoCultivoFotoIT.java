package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoAM;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.repositories.ArchivoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class TipoCultivoFotoIT extends AbstractIntegrationTest {

    @Autowired
    private TipoCultivoService tipoCultivoService;

    @Autowired
    private ArchivoService archivoService;

    @Autowired
    private ArchivoRepository archivoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * El alta y la edicion son requests distintos. Sin esto comparten el
     * contexto de persistencia del test y la entidad no se recarga.
     */
    private void simularRequestNuevo() {
        this.entityManager.flush();
        this.entityManager.clear();
    }

    @Test
    void guardaLaImagenEnElAltaYLaDevuelveConUrlDeDescarga() {
        String key = subirImagen();

        UUID id = this.tipoCultivoService
                .altaTipoCultivo(pedido(new ArchivoClaimRequest(key, "tomate.jpg")))
                .getIdTipoCultivo();

        assertThat(this.tipoCultivoService.obtenerDatosTipoCultivo(id).getFoto())
                .satisfies(foto -> {
                    assertThat(foto.getKey()).isEqualTo(key);
                    assertThat(foto.getNombre()).isEqualTo("tomate.jpg");
                    assertThat(foto.getExtension()).isEqualTo("jpg");
                    assertThat(foto.getDownloadUrl()).contains(key);
                });
    }

    /**
     * Guardar el formulario sin tocar la imagen no puede fallar: reclamar de
     * nuevo una key ya asociada la rechazaria por duplicada.
     */
    @Test
    void reenviarLaMismaKeyNoRompe() {
        String key = subirImagen();
        ArchivoClaimRequest misma = new ArchivoClaimRequest(key, "tomate.jpg");

        DTOTipoCultivoAM alta = pedido(misma);
        UUID id = this.tipoCultivoService.altaTipoCultivo(alta).getIdTipoCultivo();
        simularRequestNuevo();

        DTOTipoCultivoAM edicion = pedido(misma);
        edicion.setNombre(alta.getNombre());

        assertThatCode(() -> this.tipoCultivoService.modificarTipoCultivo(id, edicion))
                .doesNotThrowAnyException();
        assertThat(this.tipoCultivoService.obtenerDatosTipoCultivo(id).getFoto().getKey()).isEqualTo(key);
    }

    @Test
    void reemplazarLaImagenBorraElArchivoAnterior() {
        String vieja = subirImagen();
        String nueva = subirImagen();

        DTOTipoCultivoAM alta = pedido(new ArchivoClaimRequest(vieja, "vieja.jpg"));
        UUID id = this.tipoCultivoService.altaTipoCultivo(alta).getIdTipoCultivo();
        simularRequestNuevo();

        DTOTipoCultivoAM edicion = pedido(new ArchivoClaimRequest(nueva, "nueva.jpg"));
        edicion.setNombre(alta.getNombre());
        this.tipoCultivoService.modificarTipoCultivo(id, edicion);

        assertThat(this.tipoCultivoService.obtenerDatosTipoCultivo(id).getFoto().getKey()).isEqualTo(nueva);
        assertThat(this.archivoRepository.existsByKey(vieja)).isFalse();
    }

    @Test
    void mandarNullQuitaLaImagen() {
        String key = subirImagen();

        DTOTipoCultivoAM alta = pedido(new ArchivoClaimRequest(key, "tomate.jpg"));
        UUID id = this.tipoCultivoService.altaTipoCultivo(alta).getIdTipoCultivo();
        simularRequestNuevo();

        DTOTipoCultivoAM edicion = pedido(null);
        edicion.setNombre(alta.getNombre());
        this.tipoCultivoService.modificarTipoCultivo(id, edicion);

        assertThat(this.tipoCultivoService.obtenerDatosTipoCultivo(id).getFoto()).isNull();
        assertThat(this.archivoRepository.existsByKey(key)).isFalse();
    }

    @Test
    void rechazaUnaKeyDeOtraCarpeta() {
        String keyDeActividades = CarpetaArchivo.ACTIVIDADES.getPrefijo() + "/" + UUID.randomUUID() + ".jpg";

        assertThatThrownBy(() -> this.tipoCultivoService
                .altaTipoCultivo(pedido(new ArchivoClaimRequest(keyDeActividades, "x.jpg"))))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("no es una key de cultivos");
    }

    private DTOTipoCultivoAM pedido(ArchivoClaimRequest foto) {
        DTOTipoCultivoAM dto = new DTOTipoCultivoAM();
        dto.setNombre("Cultivo " + UUID.randomUUID());
        dto.setDescripcion("Cultivo de prueba");
        // Jackson siempre entrega listas mutables; armarlas inmutables aca
        // haria fallar el merge de Hibernate por un motivo que no existe en
        // produccion.
        dto.setBeneficios(new ArrayList<>(List.of("Rico en fibra")));
        dto.setEstacionalidadPorMes(new ArrayList<>(Collections.nCopies(12, EstacionalidadNombre.COSECHA)));
        dto.setPorcionReferencia("100 g");
        dto.setInformacionNutricional(new ArrayList<>());
        dto.setFoto(foto);
        return dto;
    }

    private String subirImagen() {
        ArchivoUploadRequest request = new ArchivoUploadRequest();
        request.setFilename("tomate.jpg");
        request.setFileSize(1024);

        ArchivoUploadResponse firmado = this.archivoService.getSignedArchivo(request, CarpetaArchivo.CULTIVOS);
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
