package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.establecimiento.DTOEstablecimientoAlta;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.services.EstablecimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/establecimientos")
public class EstablecimientoController extends BaseEntityControllerImpl<Establecimiento, EstablecimientoService> {
    @PostMapping("/alta")
    public ResponseEntity<?> altaEstablecimiento(@Valid @RequestBody DTOEstablecimientoAlta dto) {
        try {
            service.altaEstablecimiento(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\" Establecimiento añadido correctamente }\"");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
