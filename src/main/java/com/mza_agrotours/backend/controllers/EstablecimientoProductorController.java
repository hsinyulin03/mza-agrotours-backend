package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.CondicionDTO;
import com.mza_agrotours.backend.dtos.establecimiento.*;
import com.mza_agrotours.backend.services.EstablecimientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}")
public class EstablecimientoProductorController {

    @Autowired
    private EstablecimientoService establecimientoService;

    @PostMapping("/alta")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> altaAuxEstablecimiento(@Valid @RequestBody DTOEstablecimientoAlta dto) throws Exception {
        DTODatosEstablecimiento resultado = establecimientoService.altaAuxEstablecimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(resultado));
    }
    //US-EST-05 BM establecimiento (modificar)
    @GetMapping
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> obtenerDatosEstablecimiento(
            @PathVariable UUID establecimientoId) throws Exception {
        DTODatosEstablecimiento dto = establecimientoService.obtenerDatosEstablecimiento(establecimientoId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(dto));
    }
    // MODIFICAR ESTABLECIMIENTO
    @PutMapping
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<DTOUpdEstablecimientoResponse>> modificarEstablecimiento(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody DTOUpdEstablecimientoRequest dto) {
        DTOUpdEstablecimientoResponse resultado = establecimientoService.modificarEstablecimiento(establecimientoId, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
    // US-EST-06 BM establecimiento (baja)
    @DeleteMapping
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<DTOBajaEstablecimientoResponse>> bajaEstablecimiento(
            @PathVariable UUID establecimientoId) {

        DTOBajaEstablecimientoResponse resultado = establecimientoService.bajaEstablecimiento(establecimientoId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }

    @GetMapping("/condiciones-baja")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<List<CondicionDTO>>> getCondicionesBajaEstablecimiento(@PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(establecimientoService.getCondicionesDeleteEstablecimiento(establecimientoId)));
    }

}
