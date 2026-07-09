package com.mza_agrotours.backend.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExecptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {

        Map<String, String> errores = new HashMap<>();

        // Recorremos todos los errores de validación
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            // Guardamos el nombre del campo y su mensaje de error ("El nombre es obligatorio")
            errores.put(error.getField(), error.getDefaultMessage());
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseExceptions(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();

        // La base de datos lanza un error complejo, así que le ponemos un mensaje amigable
        error.put("error", "Ya existe un registro con ese dato en el sistema.");

        // Usamos el código 409 (Conflict) porque hay un conflicto con los datos existentes
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}
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
