package com.mza_agrotours.backend.controllers;


import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.establecimiento.DTOCatalogoEstablecimientoVisitante;
import com.mza_agrotours.backend.dtos.establecimiento.DTODetalleEstablecimientoVisitantes;
import com.mza_agrotours.backend.dtos.establecimiento.DTOFiltroCultivoEstablecimiento;
import com.mza_agrotours.backend.dtos.establecimiento.DTOFiltroDepartamentoEstablecimiento;
import com.mza_agrotours.backend.services.EstablecimientoService;
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
public class EstablecimientoVisitanteController {

    @Autowired
    private EstablecimientoService establecimientoService;

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

    //  US-EST-02 Consultar establecimiento
    @GetMapping("/{id}/detalle")
    public ResponseEntity<ApiResponse<DTODetalleEstablecimientoVisitantes>> obtenerDetalleEstablecimientoVisitante(
            @PathVariable UUID id) {
        DTODetalleEstablecimientoVisitantes dto = establecimientoService.obtenerDetalleEstablecimientoVisitante(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(dto));
    }

}
