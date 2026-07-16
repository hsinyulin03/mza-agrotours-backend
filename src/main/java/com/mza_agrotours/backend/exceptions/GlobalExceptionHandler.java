package com.mza_agrotours.backend.exceptions;

import com.mza_agrotours.backend.dtos.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNotFound.class)
    public ResponseEntity<?> handleUserNotFoundException(UsuarioNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("badCreds", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(UsuarioAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("userAlreadyExists", ex.getMessage()));
    }

    @ExceptionHandler(TipoIdentificacionInvalidoException.class)
    public ResponseEntity<?> handleTipoIdentificacionInvalido(TipoIdentificacionInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("tipoIdentificacionInvalido", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioDeactivatedException.class)
    public ResponseEntity<?> handleUsuarioDeactivated(UsuarioDeactivatedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("usuarioDeBaja", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("validationError", "Datos de entrada invalidos", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("badRequest", "Parametros invalidos", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String mensaje = "Parámetro '%s' invalido".formatted(ex.getName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("invalidParameter", mensaje));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("internalServerError", ex.getMessage()));
    }

}
