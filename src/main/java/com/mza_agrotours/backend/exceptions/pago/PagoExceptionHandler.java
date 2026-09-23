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
}