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
}
