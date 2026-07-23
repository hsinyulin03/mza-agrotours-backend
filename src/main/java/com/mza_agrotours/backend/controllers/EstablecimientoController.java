package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.establecimiento.*;
import com.mza_agrotours.backend.services.EstablecimientoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos")
public class EstablecimientoController {

    @Autowired
    private EstablecimientoService establecimientoService;
    @PostMapping("/alta")
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> altaEstablecimiento(@Valid @RequestBody DTOEstablecimientoAlta dto) throws Exception {
        DTODatosEstablecimiento resultado = establecimientoService.altaEstablecimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(resultado));
    }
    //US-EST-05 BM establecimiento (modificar)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> obtenerDatosEstablecimiento(
            @PathVariable UUID id) throws Exception {
        DTODatosEstablecimiento dto = establecimientoService.obtenerDatosEstablecimiento(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(dto));
    }
    // MODIFICAR ESTABLECIMIENTO
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DTODatosEstablecimiento>> modificarEstablecimiento(
            @PathVariable UUID id,
            @Valid @RequestBody DTODatosEstablecimientoUpd dto) {
        DTODatosEstablecimiento resultado = establecimientoService.modificarEstablecimiento(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
    // US-EST-06 BM establecimiento (baja)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> bajaEstablecimiento(
            @PathVariable UUID id) {

        establecimientoService.bajaEstablecimiento(id);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(null));
    }
    //US-EST-01 Consultar establecimientos
    @GetMapping
    public ResponseEntity<ApiResponse<List<DTOConsultarEstablecimientoSVisitante>>> consultarEstablecimientosVisitantes() {
        List<DTOConsultarEstablecimientoSVisitante> establecimientos = establecimientoService.consultarEstablecimientosVisitantes();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(establecimientos));
    }
    //  US-EST-02 Consultar establecimiento
    @GetMapping("/{id}/detalle")
    public ResponseEntity<ApiResponse<DTODetalleEstablecimientoVisitantes>> obtenerDetalleEstablecimientoVisitante(
            @PathVariable UUID id) {
        DTODetalleEstablecimientoVisitantes dto = establecimientoService.obtenerDetalleEstablecimientoVisitante(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(dto));
    }


}
