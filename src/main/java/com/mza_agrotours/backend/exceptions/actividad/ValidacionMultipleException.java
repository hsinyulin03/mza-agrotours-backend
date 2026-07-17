package com.mza_agrotours.backend.exceptions.actividad;

import java.util.List;

public class ValidacionMultipleException extends RuntimeException {
    private final List<String> errores;

    public ValidacionMultipleException(List<String> errores) {
        // Le pasamos un mensaje genérico al padre
        super("Se encontraron múltiples errores de validación al procesar la solicitud.");
        this.errores = errores;
    }

    public List<String> getErrores() {
        return errores;
    }
}
