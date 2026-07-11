package com.mza_agrotours.backend.exceptions;

import com.mza_agrotours.backend.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNotFound.class)
    public ResponseEntity<?> handleUserNotFoundException(UsuarioNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("badCreds"));
    }

    @ExceptionHandler(UsuarioAlreadyExistsException.class)
    public ResponseEntity<?> handleUserAlreadyExistsException(UsuarioAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("userAlreadyExists"));
    }

    @ExceptionHandler(TipoIdentificacionInvalidoException.class)
    public ResponseEntity<?> handleTipoIdentificacionInvalido(TipoIdentificacionInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("tipoIdentificacionInvalido"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail("internalServerError"));
    }

}
