package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.establecimiento.DTODatosEstablecimiento;
import com.mza_agrotours.backend.dtos.establecimiento.DTOEstablecimientoAlta;
import com.mza_agrotours.backend.services.EstablecimientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/establecimientos")
public class EstablecimientoController {
    @Autowired
    private EstablecimientoService establecimientoService;
    @PostMapping("/alta")
    public ResponseEntity<ApiResponse<Void>> altaEstablecimiento(@Valid @RequestBody DTOEstablecimientoAlta dto) throws Exception {
        establecimientoService.altaEstablecimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> obtenerDatosEstablecimiento(
            @PathVariable UUID id) throws Exception {
        DTODatosEstablecimiento dto = establecimientoService.obtenerDatosEstablecimiento(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(dto));
    }
}
