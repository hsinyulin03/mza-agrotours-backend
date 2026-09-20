package com.mza_agrotours.backend.enums;

import lombok.Getter;

import java.util.List;

/**
 * Cada carpeta agrupa los objetos de un contexto y define que puede entrar en
 * ella. El prefijo forma parte de la key y no cambia: hay objetos guardados
 * con el.
 */
@Getter
public enum CarpetaArchivo {
    ACTIVIDADES("actividades", List.of("jpg", "jpeg", "png"), 5L * 1024 * 1024),
    ESTABLECIMIENTOS("establecimientos", List.of("jpg", "jpeg", "png"), 5L * 1024 * 1024),
    SOLICITUDES_ESTABLECIMIENTO("solicitudes-establecimiento", List.of("pdf", "jpg", "jpeg", "png"), 10L * 1024 * 1024);

    private final String prefijo;
    private final List<String> extensionesPermitidas;
    private final long maxFileSize;

    CarpetaArchivo(String prefijo, List<String> extensionesPermitidas, long maxFileSize) {
        this.prefijo = prefijo;
        this.extensionesPermitidas = extensionesPermitidas;
        this.maxFileSize = maxFileSize;
    }

    public boolean permite(String extension) {
        return extension != null && this.extensionesPermitidas.contains(extension.toLowerCase());
    }

    public boolean contiene(String key) {
        return key != null && key.startsWith(this.prefijo + "/");
    }
}
