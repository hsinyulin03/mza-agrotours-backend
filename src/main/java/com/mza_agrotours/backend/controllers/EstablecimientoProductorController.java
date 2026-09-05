package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.establecimiento.*;
import com.mza_agrotours.backend.services.EstablecimientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}")
public class EstablecimientoProductorController {

    @Autowired
    private EstablecimientoService establecimientoService;
    @PostMapping("/alta")
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> altaAuxEstablecimiento(@Valid @RequestBody DTOEstablecimientoAlta dto) throws Exception {
        DTODatosEstablecimiento resultado = establecimientoService.altaAuxEstablecimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(resultado));
    }
    //US-EST-05 BM establecimiento (modificar)
    @GetMapping
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> obtenerDatosEstablecimiento(
            @PathVariable UUID establecimientoId) throws Exception {
        DTODatosEstablecimiento dto = establecimientoService.obtenerDatosEstablecimiento(establecimientoId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(dto));
    }
    // MODIFICAR ESTABLECIMIENTO
    @PutMapping
    public ResponseEntity<ApiResponse<DTOUpdEstablecimientoResponse>> modificarEstablecimiento(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody DTOUpdEstablecimientoRequest dto) {
        DTOUpdEstablecimientoResponse resultado = establecimientoService.modificarEstablecimiento(establecimientoId, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
    // US-EST-06 BM establecimiento (baja)
    @DeleteMapping
    public ResponseEntity<ApiResponse<DTOBajaEstablecimientoResponse>> bajaEstablecimiento(
            @PathVariable UUID establecimientoId) {

        DTOBajaEstablecimientoResponse resultado = establecimientoService.bajaEstablecimiento(establecimientoId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }

}
