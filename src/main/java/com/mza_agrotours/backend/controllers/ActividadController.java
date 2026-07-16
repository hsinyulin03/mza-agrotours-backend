package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.actividad.DTOActividadAlta;
import com.mza_agrotours.backend.services.ActividadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/actividad")
public class ActividadController {
    private final ActividadService servicio;

    public ActividadController(ActividadService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/alta")
    public ResponseEntity<?> crearActividadConDetalles(@Valid @RequestBody DTOActividadAlta dto) {

        try {
            servicio.AltaActividad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\" Actividad añadido correctamente }\"");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }

    }

}
