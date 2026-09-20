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
    ACTIVIDADES("actividades", List.of("jpg", "jpeg", "png")),
    ESTABLECIMIENTOS("establecimientos", List.of("jpg", "jpeg", "png")),
    SOLICITUDES_ESTABLECIMIENTO("solicitudes-establecimiento", List.of("pdf", "jpg", "jpeg", "png"));

    private final String prefijo;
    private final List<String> extensionesPermitidas;

    CarpetaArchivo(String prefijo, List<String> extensionesPermitidas) {
        this.prefijo = prefijo;
        this.extensionesPermitidas = extensionesPermitidas;
    }

    public boolean permite(String extension) {
        return extension != null && this.extensionesPermitidas.contains(extension.toLowerCase());
    }
}
