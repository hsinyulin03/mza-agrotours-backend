package com.mza_agrotours.backend.exceptions.reservas;

import com.mza_agrotours.backend.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ReservaExceptionHandler {

    @ExceptionHandler(ReservaNotFoundException.class)
    public ResponseEntity<?> handleReservaNotFoundException(ReservaNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("notFound", ex.getMessage()));
    }

    @ExceptionHandler(ActividadFullException.class)
    public ResponseEntity<?> handleActividadFullException(ActividadFullException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("activityFull", ex.getMessage()));
    }

    @ExceptionHandler(EstadoReservaNotFoundException.class)
    public ResponseEntity<?> handleReservaEstadoNotFoundException(EstadoReservaNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail("activityFull", ex.getMessage()));
    }
}
