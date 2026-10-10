package com.mza_agrotours.backend.exceptions.pago;

import com.mza_agrotours.backend.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PagoExceptionHandler {

    @ExceptionHandler(EstadoPagoNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEstadoPagoNotFoundException(EstadoPagoNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("estadoNotFound", ex.getMessage()));
    }

    @ExceptionHandler(EstadoReembolsoNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEstadoReembolsoNotFoundException(EstadoReembolsoNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("estadoNotFound", ex.getMessage()));
    }

    @ExceptionHandler(ReembolsoStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleReembolsoStateException(ReembolsoStateException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("forbiddenState", ex.getMessage()));
    }

    @ExceptionHandler(ReembolsoDateException.class)
    public ResponseEntity<ApiResponse<Void>> handleReembolsoDateException(ReembolsoDateException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("forbiddenDate", ex.getMessage()));
    }

    @ExceptionHandler(EstablecimientoSinCuentaMercadoPagoException.class)
    public ResponseEntity<ApiResponse<Void>> handleEstablecimientoSinCuentaMercadoPagoException(EstablecimientoSinCuentaMercadoPagoException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.fail("establecimientoSinCuentaMP", ex.getMessage()));
    }
}