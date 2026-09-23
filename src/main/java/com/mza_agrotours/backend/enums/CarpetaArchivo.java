package com.mza_agrotours.backend.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Cada carpeta agrupa los objetos de un contexto y define que puede entrar en
 * ella. El prefijo forma parte de la key y no cambia: hay objetos guardados
 * con el.
 * <p>
 * Una carpeta publica es la que el bucket sirve a cualquiera por GET anonimo,
 * sin firma. Marcarla aca no la abre por si sola: la abre la policy del bucket
 * ({@link com.mza_agrotours.backend.services.ObjectStoragePolicies}), que se
 * aplica por fuera de la aplicacion.
 */
@Getter
public enum CarpetaArchivo {
    ACTIVIDADES("actividades", List.of("jpg", "jpeg", "png"), 5L * 1024 * 1024, true),
    ESTABLECIMIENTOS("establecimientos", List.of("jpg", "jpeg", "png"), 5L * 1024 * 1024, true),
    CULTIVOS("cultivos", List.of("jpg", "jpeg", "png"), 5L * 1024 * 1024, true),
    RECETAS("recetas", List.of("jpg", "jpeg", "png"), 5L * 1024 * 1024, true),
    SOLICITUDES_ESTABLECIMIENTO("solicitudes-establecimiento", List.of("pdf", "jpg", "jpeg", "png"), 10L * 1024 * 1024, false);

    private final String prefijo;
    private final List<String> extensionesPermitidas;
    private final long maxFileSize;
    private final boolean publica;

    CarpetaArchivo(String prefijo, List<String> extensionesPermitidas, long maxFileSize, boolean publica) {
        this.prefijo = prefijo;
        this.extensionesPermitidas = extensionesPermitidas;
        this.maxFileSize = maxFileSize;
        this.publica = publica;
    }

    /**
     * Las keys planas anteriores a las carpetas no caen en ninguna, y por eso
     * se siguen sirviendo firmadas.
     */
    public static Optional<CarpetaArchivo> de(String key) {
        return Arrays.stream(values())
                .filter(carpeta -> carpeta.contiene(key))
                .findFirst();
    }

    public static List<CarpetaArchivo> publicas() {
        return Arrays.stream(values())
                .filter(CarpetaArchivo::isPublica)
                .toList();
    }

    public boolean permite(String extension) {
        return extension != null && this.extensionesPermitidas.contains(extension.toLowerCase());
    }

    public boolean contiene(String key) {
        return key != null && key.startsWith(this.prefijo + "/");
    }
}
