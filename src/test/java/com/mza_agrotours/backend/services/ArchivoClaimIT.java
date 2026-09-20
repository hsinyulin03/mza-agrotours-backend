package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.repositories.ArchivoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * El claim es el unico punto donde el backend confia en una key que le mando
 * el cliente. Todo lo que se verifica aca es lo que antes se daba por cierto
 * porque la key se generaba en la misma llamada.
 */
class ArchivoClaimIT extends AbstractIntegrationTest {

    @Autowired
    private ArchivoService archivoService;

    @Autowired
    private ArchivoRepository archivoRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void reclamaUnaKeyRealmenteSubida() {
        String key = subirArchivo("foto.jpg", CarpetaArchivo.ACTIVIDADES, 1024);

        Archivo archivo = this.archivoService.reclamarArchivo(
                new ArchivoClaimRequest(key, "mi vacacion.jpg"), CarpetaArchivo.ACTIVIDADES);

        assertThat(archivo.getKey()).isEqualTo(key);
        assertThat(archivo.getNombre()).isEqualTo("mi vacacion.jpg");
        assertThat(archivo.getExtension()).isEqualTo("jpg");
    }

    @Test
    void rechazaUnaKeyDeOtraCarpeta() {
        String key = subirArchivo("prueba.pdf", CarpetaArchivo.SOLICITUDES_ESTABLECIMIENTO, 1024);

        assertThatThrownBy(() -> this.archivoService.reclamarArchivo(
                new ArchivoClaimRequest(key, "prueba.pdf"), CarpetaArchivo.ACTIVIDADES))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("no es una key de actividades");
    }

    @Test
    void rechazaUnaKeyQueNuncaSeSubio() {
        String key = CarpetaArchivo.ACTIVIDADES.getPrefijo() + "/" + UUID.randomUUID() + ".jpg";

        assertThatThrownBy(() -> this.archivoService.reclamarArchivo(
                new ArchivoClaimRequest(key, "fantasma.jpg"), CarpetaArchivo.ACTIVIDADES))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rechazaUnaKeyYaReclamadaPorOtroRegistro() {
        String key = subirArchivo("foto.jpg", CarpetaArchivo.ACTIVIDADES, 1024);
        this.archivoRepository.save(new Archivo(key, "primera.jpg", "jpg"));

        assertThatThrownBy(() -> this.archivoService.reclamarArchivo(
                new ArchivoClaimRequest(key, "robada.jpg"), CarpetaArchivo.ACTIVIDADES))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("ya fue asociado");
    }

    /**
     * El cliente declara el tamanio al pedir la url y puede mentir: la url
     * prefirmada no impone limite. Esto es lo que cierra ese agujero.
     */
    @Test
    void rechazaUnObjetoQueEnElBucketPesaMasDeLoPermitido() {
        ArchivoUploadResponse firmado = this.archivoService.getSignedArchivo(
                pedido("gigante.jpg", 1024), CarpetaArchivo.ACTIVIDADES);

        byte[] gordo = new byte[(int) CarpetaArchivo.ACTIVIDADES.getMaxFileSize() + 1];
        assertThat(subir(firmado, gordo)).isEqualTo(200);

        assertThatThrownBy(() -> this.archivoService.reclamarArchivo(
                new ArchivoClaimRequest(firmado.getKey(), "gigante.jpg"), CarpetaArchivo.ACTIVIDADES))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("supera el maximo");
    }

    @Test
    void noFirmaSiElTamanioDeclaradoYaExcedeLaCarpeta() {
        assertThatThrownBy(() -> this.archivoService.getSignedArchivo(
                pedido("grande.jpg", CarpetaArchivo.ACTIVIDADES.getMaxFileSize() + 1), CarpetaArchivo.ACTIVIDADES))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("tamanio maximo");
    }

    @Test
    void reclamarUnaListaVaciaNoFalla() {
        assertThat(this.archivoService.reclamarArchivos(null, CarpetaArchivo.ACTIVIDADES)).isEmpty();
        assertThat(this.archivoService.reclamarArchivos(List.of(), CarpetaArchivo.ACTIVIDADES)).isEmpty();
    }

    private ArchivoUploadRequest pedido(String filename, long fileSize) {
        ArchivoUploadRequest request = new ArchivoUploadRequest();
        request.setFilename(filename);
        request.setFileSize(fileSize);
        return request;
    }

    private String subirArchivo(String filename, CarpetaArchivo carpeta, int bytes) {
        ArchivoUploadResponse firmado = this.archivoService.getSignedArchivo(pedido(filename, bytes), carpeta);
        assertThat(subir(firmado, new byte[bytes])).isEqualTo(200);
        return firmado.getKey();
    }

    private int subir(ArchivoUploadResponse firmado, byte[] contenido) {
        try {
            return this.httpClient.send(
                    HttpRequest.newBuilder(URI.create(firmado.getUploadUrl()))
                            .header("Content-Type", firmado.getContentType())
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(contenido))
                            .build(),
                    HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
