package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.config.ObjectStorageProperties;
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
 * disco no podia verificar, porque no firmaba nada. El bucket de los tests
 * lleva la misma policy que el de produccion, asi que tambien verifica que
 * carpetas quedan abiertas a lectura anonima y cuales no.
 */
class ObjectStorageIT extends AbstractIntegrationTest {

    @Autowired
    private ArchivoService archivoService;

    @Autowired
    private ObjectStorageProperties properties;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void subeYDescargaUnArchivo() throws Exception {
        byte[] contenido = "contenido de prueba".getBytes(StandardCharsets.UTF_8);
        ArchivoUploadResponse archivo = firmar("foto.jpg", contenido.length, CarpetaArchivo.ACTIVIDADES);

        assertThat(archivo.getContentType()).isEqualTo("image/jpeg");
        assertThat(archivo.getExtension()).isEqualTo("jpg");
        assertThat(archivo.getNombre()).isEqualTo("foto.jpg");

        assertThat(subir(archivo.getUploadUrl(), archivo.getContentType(), contenido)).isEqualTo(200);

        HttpResponse<byte[]> descarga = descargar(this.archivoService.getDownloadUrl(archivo.getKey()));

        assertThat(descarga.statusCode()).isEqualTo(200);
        assertThat(descarga.body()).isEqualTo(contenido);
        assertThat(descarga.headers().firstValue("content-type")).hasValue("image/jpeg");
    }

    @Test
    void sirveSinFirmaLoQueCaeEnUnaCarpetaPublica() throws Exception {
        byte[] contenido = "foto de una actividad".getBytes(StandardCharsets.UTF_8);
        ArchivoUploadResponse archivo = firmar("foto.jpg", contenido.length, CarpetaArchivo.ACTIVIDADES);
        assertThat(subir(archivo.getUploadUrl(), archivo.getContentType(), contenido)).isEqualTo(200);

        String url = this.archivoService.getDownloadUrl(archivo.getKey());

        assertThat(url).endsWith("/" + archivo.getKey()).doesNotContain("X-Amz-Signature");
        assertThat(descargar(url).statusCode()).isEqualTo(200);
    }

    @Test
    void firmaLoQueCaeEnUnaCarpetaPrivada() throws Exception {
        byte[] contenido = "una solicitud reservada".getBytes(StandardCharsets.UTF_8);
        ArchivoUploadResponse archivo = firmar("solicitud.pdf", contenido.length,
                CarpetaArchivo.SOLICITUDES_ESTABLECIMIENTO);
        assertThat(subir(archivo.getUploadUrl(), archivo.getContentType(), contenido)).isEqualTo(200);

        String url = this.archivoService.getDownloadUrl(archivo.getKey());

        assertThat(url).contains("X-Amz-Signature");
        assertThat(descargar(url).statusCode()).isEqualTo(200);
        assertThat(descargar(url.substring(0, url.indexOf('?'))).statusCode()).isEqualTo(403);
    }

    @Test
    void ningunaCarpetaQuedaListableSinCredenciales() throws Exception {
        HttpResponse<byte[]> listado = descargar(
                this.properties.getPublicBaseUrl() + "?list-type=2&prefix=actividades/");

        assertThat(listado.statusCode()).isEqualTo(403);
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
    void sigueSirviendoFirmadasLasKeysPlanasAnterioresALasCarpetas() {
        String keyVieja = UUID.randomUUID() + ".jpg";

        assertThatCode(() -> this.archivoService.getDownloadUrl(keyVieja)).doesNotThrowAnyException();
        assertThat(this.archivoService.getDownloadUrl(keyVieja)).contains("X-Amz-Signature");
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

    private HttpResponse<byte[]> descargar(String url) throws Exception {
        return this.httpClient.send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
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
