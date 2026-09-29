package com.mza_agrotours.backend.exceptions.pago;

import com.mza_agrotours.backend.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PagoExceptionHandler {

    @ExceptionHandler(EstadoPagoNotFoundException.class)
    public ResponseEntity<?> handleEstadoPagoNotFoundException(EstadoPagoNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("estadoNotFound", ex.getMessage()));
    }

    @ExceptionHandler(EstadoReembolsoNotFoundException.class)
    public ResponseEntity<?> handleEstadoReembolsoNotFoundException(EstadoReembolsoNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("estadoNotFound", ex.getMessage()));
    }

    @ExceptionHandler(ReembolsoMakingException.class)
    public ResponseEntity<?> handleReembolsoMakingException(ReembolsoMakingException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("reembolsoImpossible", ex.getMessage()));
    }

    @ExceptionHandler(ReembolsoStateException.class)
    public ResponseEntity<?> handleReembolsoStateException(ReembolsoStateException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("forbiddenState", ex.getMessage()));
    }

    @ExceptionHandler(ReembolsoDateException.class)
    public ResponseEntity<?> handleReembolsoDateException(ReembolsoDateException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("forbiddenDate", ex.getMessage()));
    }
}