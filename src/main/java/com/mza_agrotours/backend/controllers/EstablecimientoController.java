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
    public ResponseEntity<ApiResponse<Page<DTOCatalogoEstablecimientoVisitante>>> getCatalogo(
            @RequestParam(required = false) List<UUID> cultivosIds,
            @RequestParam(required = false) UUID departamentoId,
            @PageableDefault(page = 0, size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<DTOCatalogoEstablecimientoVisitante> catalogo = establecimientoService
                .consultarEstablecimientosVisitantes(cultivosIds, departamentoId, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(catalogo));
    }
    @GetMapping("/filtros/cultivos")
    public ResponseEntity<ApiResponse<List<DTOFiltroCultivoEstablecimiento>>> obtenerFiltroCultivos() {
        List<DTOFiltroCultivoEstablecimiento> filtros = establecimientoService.obtenerFiltroCultivos();
        return ResponseEntity.ok(ApiResponse.ok(filtros));
    }
    @GetMapping("/filtros/departamentos")
    public ResponseEntity<ApiResponse<List<DTOFiltroDepartamentoEstablecimiento>>> obtenerFiltroDepartamentos() {
        List<DTOFiltroDepartamentoEstablecimiento> filtros = establecimientoService.obtenerFiltroDepartamentos();
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
