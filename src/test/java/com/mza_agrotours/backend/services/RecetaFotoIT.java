package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.receta.DTORecetaAM;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.enums.Dificultad;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.repositories.ArchivoRepository;
import com.mza_agrotours.backend.repositories.TipoCultivo.TipoCultivoRepository;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class RecetaFotoIT extends AbstractIntegrationTest {

    @Autowired
    private RecetaService recetaService;

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;

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

        UUID id = this.recetaService
                .altaReceta(pedido(new ArchivoClaimRequest(key, "tarta.jpg")))
                .getIdReceta();

        assertThat(this.recetaService.obtenerDatosReceta(id).getFoto())
                .satisfies(foto -> {
                    assertThat(foto.getKey()).isEqualTo(key);
                    assertThat(foto.getNombre()).isEqualTo("tarta.jpg");
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
        ArchivoClaimRequest misma = new ArchivoClaimRequest(key, "tarta.jpg");

        DTORecetaAM alta = pedido(misma);
        UUID id = this.recetaService.altaReceta(alta).getIdReceta();
        simularRequestNuevo();

        DTORecetaAM edicion = pedido(misma);
        edicion.setNombre(alta.getNombre());

        assertThatCode(() -> this.recetaService.modificarReceta(id, edicion))
                .doesNotThrowAnyException();
        assertThat(this.recetaService.obtenerDatosReceta(id).getFoto().getKey()).isEqualTo(key);
    }

    @Test
    void reemplazarLaImagenBorraElArchivoAnterior() {
        String vieja = subirImagen();
        String nueva = subirImagen();

        DTORecetaAM alta = pedido(new ArchivoClaimRequest(vieja, "vieja.jpg"));
        UUID id = this.recetaService.altaReceta(alta).getIdReceta();
        simularRequestNuevo();

        DTORecetaAM edicion = pedido(new ArchivoClaimRequest(nueva, "nueva.jpg"));
        edicion.setNombre(alta.getNombre());
        this.recetaService.modificarReceta(id, edicion);

        assertThat(this.recetaService.obtenerDatosReceta(id).getFoto().getKey()).isEqualTo(nueva);
        assertThat(this.archivoRepository.existsByKey(vieja)).isFalse();
    }

    @Test
    void mandarNullQuitaLaImagen() {
        String key = subirImagen();

        DTORecetaAM alta = pedido(new ArchivoClaimRequest(key, "tarta.jpg"));
        UUID id = this.recetaService.altaReceta(alta).getIdReceta();
        simularRequestNuevo();

        DTORecetaAM edicion = pedido(null);
        edicion.setNombre(alta.getNombre());
        this.recetaService.modificarReceta(id, edicion);

        assertThat(this.recetaService.obtenerDatosReceta(id).getFoto()).isNull();
        assertThat(this.archivoRepository.existsByKey(key)).isFalse();
    }

    @Test
    void rechazaUnaKeyDeOtraCarpeta() {
        String keyDeCultivos = CarpetaArchivo.CULTIVOS.getPrefijo() + "/" + UUID.randomUUID() + ".jpg";

        assertThatThrownBy(() -> this.recetaService
                .altaReceta(pedido(new ArchivoClaimRequest(keyDeCultivos, "x.jpg"))))
                .isInstanceOf(DatoInvalidoException.class)
                .hasMessageContaining("no es una key de recetas");
    }

    private DTORecetaAM pedido(ArchivoClaimRequest foto) {
        DTORecetaAM dto = new DTORecetaAM();
        dto.setNombre("Receta " + UUID.randomUUID());
        // Jackson siempre entrega listas mutables; armarlas inmutables aca
        // haria fallar el merge de Hibernate por un motivo que no existe en
        // produccion.
        dto.setCultivosIds(new ArrayList<>(List.of(crearCultivo())));
        dto.setDificultad(Dificultad.FACIL);
        dto.setTiempoMinsAprox(30);
        dto.setPorciones(4);
        dto.setDescripcion("Receta de prueba");
        dto.setIngredientes(new ArrayList<>(List.of("2 tomates")));
        dto.setPasos(new ArrayList<>(List.of("Cortar los tomates")));
        dto.setFoto(foto);
        return dto;
    }

    private UUID crearCultivo() {
        TipoCultivo cultivo = new TipoCultivo();
        cultivo.setNombre("Cultivo " + UUID.randomUUID());
        cultivo.setDescripcion("Cultivo de prueba");
        cultivo.setPorcionReferencia("100 g");
        return this.tipoCultivoRepository.save(cultivo).getId();
    }

    private String subirImagen() {
        ArchivoUploadRequest request = new ArchivoUploadRequest();
        request.setFilename("tarta.jpg");
        request.setFileSize(1024);

        ArchivoUploadResponse firmado = this.archivoService.getSignedArchivo(request, CarpetaArchivo.RECETAS);
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
