package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ejercita el presigning contra un MinIO real: es lo que el proveedor local en
 * disco no podia verificar, porque no firmaba nada.
 */
class ObjectStorageIT extends AbstractIntegrationTest {

    @Autowired
    private ArchivoService archivoService;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void subeYDescargaUnArchivoConUrlsPrefirmadas() throws Exception {
        byte[] contenido = "contenido de prueba".getBytes(StandardCharsets.UTF_8);
        ArchivoUploadResponse archivo = firmar("foto.jpg", contenido.length, CarpetaArchivo.ACTIVIDADES);

        assertThat(archivo.getContentType()).isEqualTo("image/jpeg");
        assertThat(archivo.getExtension()).isEqualTo("jpg");
        assertThat(archivo.getNombre()).isEqualTo("foto.jpg");

        assertThat(subir(archivo.getUploadUrl(), archivo.getContentType(), contenido)).isEqualTo(200);

        HttpResponse<byte[]> descarga = this.httpClient.send(
                HttpRequest.newBuilder(URI.create(this.archivoService.getDownloadUrl(archivo.getKey()))).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());

        assertThat(descarga.statusCode()).isEqualTo(200);
        assertThat(descarga.body()).isEqualTo(contenido);
        assertThat(descarga.headers().firstValue("content-type")).hasValue("image/jpeg");
    }

    @Test
    void guardaCadaArchivoBajoElPrefijoDeSuCarpeta() {
        assertThat(firmar("foto.jpg", 10, CarpetaArchivo.ACTIVIDADES).getKey())
                .startsWith("actividades/")
                .endsWith(".jpg");

        assertThat(firmar("prueba.pdf", 10, CarpetaArchivo.SOLICITUDES_ESTABLECIMIENTO).getKey())
                .startsWith("solicitudes-establecimiento/")
                .endsWith(".pdf");
    }

    @Test
    void cadaCarpetaAceptaSoloSusExtensiones() {
        assertThatCode(() -> firmar("prueba.pdf", 10, CarpetaArchivo.SOLICITUDES_ESTABLECIMIENTO))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> firmar("prueba.pdf", 10, CarpetaArchivo.ACTIVIDADES))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("extension no permitida en actividades");
    }

    @Test
    void rechazaElUploadSiElContentTypeNoCoincideConElFirmado() throws Exception {
        ArchivoUploadResponse archivo = firmar("foto.png", 10, CarpetaArchivo.ACTIVIDADES);

        assertThat(subir(archivo.getUploadUrl(), "text/html", "<script>".getBytes(StandardCharsets.UTF_8)))
                .isEqualTo(403);
    }

    @Test
    void noFirmaUnArchivoQueSuperaElTamanioMaximo() {
        assertThatThrownBy(() -> firmar("foto.jpg", 10485761L, CarpetaArchivo.ACTIVIDADES))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("tamanio maximo");
    }

    @Test
    void sigueSirviendoLasKeysPlanasAnterioresALasCarpetas() {
        String keyVieja = UUID.randomUUID() + ".jpg";

        assertThatCode(() -> this.archivoService.getDownloadUrl(keyVieja)).doesNotThrowAnyException();
    }

    @Test
    void rechazaKeysQueNoSalieronDelServidor() {
        assertThatThrownBy(() -> this.archivoService.getDownloadUrl("../../etc/passwd"))
                .isInstanceOf(DatoInvalidoException.class);

        assertThatThrownBy(() -> this.archivoService.getDownloadUrl("otra-carpeta/" + UUID.randomUUID() + ".jpg"))
                .isInstanceOf(DatoInvalidoException.class);
    }

    private ArchivoUploadResponse firmar(String filename, long fileSize, CarpetaArchivo carpeta) {
        ArchivoUploadRequest request = new ArchivoUploadRequest();
        request.setFilename(filename);
        request.setFileSize(fileSize);
        return this.archivoService.getSignedArchivo(request, carpeta);
    }

    private int subir(String uploadUrl, String contentType, byte[] contenido) throws Exception {
        return this.httpClient.send(
                HttpRequest.newBuilder(URI.create(uploadUrl))
                        .header("Content-Type", contentType)
                        .PUT(HttpRequest.BodyPublishers.ofByteArray(contenido))
                        .build(),
                HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
