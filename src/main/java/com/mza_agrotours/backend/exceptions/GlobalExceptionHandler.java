package com.mza_agrotours.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {

        Map<String, String> errores = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            String campo = error.getField();
            String mensaje = error.getDefaultMessage();
            String tipoValidacion = error.getCode(); // Nos dice si falló un NotBlank, Size, Pattern, etc.

            // Si el campo AÚN NO tiene un error guardado, lo guardamos
            if (!errores.containsKey(campo)) {
                errores.put(campo, mensaje);
            } else {
                // Si ya había un error, pero el NUEVO error es porque el campo está vacío,
                // lo "pisamos" porque decirle que es requerido es más importante.
                if ("NotBlank".equals(tipoValidacion) || "NotNull".equals(tipoValidacion)) {
                    errores.put(campo, mensaje);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}
