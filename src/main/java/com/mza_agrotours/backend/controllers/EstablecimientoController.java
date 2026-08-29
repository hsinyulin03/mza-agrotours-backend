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
    public ResponseEntity<ApiResponse<DTOUpdEstablecimientoResponse>> modificarEstablecimiento(
            @PathVariable UUID id,
            @Valid @RequestBody DTOUpdEstablecimientoRequest dto) {
        DTOUpdEstablecimientoResponse resultado = establecimientoService.modificarEstablecimiento(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
    // US-EST-06 BM establecimiento (baja)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOBajaEstablecimientoResponse>> bajaEstablecimiento(
            @PathVariable UUID id) {

        DTOBajaEstablecimientoResponse resultado = establecimientoService.bajaEstablecimiento(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
    //US-EST-01 Consultar establecimientos
    @GetMapping("/catalogo")
    public ResponseEntity<ApiResponse<List<DTOCatalogoEstablecimientoVisitante>>> consultarEstablecimientosVisitantes(
            @RequestParam(required = false) UUID cultivoId) {
        List<DTOCatalogoEstablecimientoVisitante> resultado = establecimientoService.consultarEstablecimientosVisitantes(cultivoId);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
    @GetMapping("/filtros/cultivos")
    public ResponseEntity<ApiResponse<List<DTOFiltroCultivoEstablecimiento>>> obtenerFiltroCultivos() {
        List<DTOFiltroCultivoEstablecimiento> filtros = establecimientoService.obtenerFiltroCultivos();
        return ResponseEntity.ok(ApiResponse.ok(filtros));
    }
    /*
    //  US-EST-02 Consultar establecimiento
    @GetMapping("/{id}/detalle")
    public ResponseEntity<ApiResponse<DTODetalleEstablecimientoVisitantes>> obtenerDetalleEstablecimientoVisitante(
            @PathVariable UUID id) {
        DTODetalleEstablecimientoVisitantes dto = establecimientoService.obtenerDetalleEstablecimientoVisitante(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(dto));
    }*/


}
