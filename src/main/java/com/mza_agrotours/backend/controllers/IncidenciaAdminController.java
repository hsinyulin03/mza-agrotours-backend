package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.incidencia.*;
import com.mza_agrotours.backend.services.IncidenciaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/incidencias")
public class IncidenciaAdminController {
    @Autowired
    private IncidenciaService incidenciaService;
    @GetMapping("/filtros/estados")
    public ResponseEntity<ApiResponse<IncidenciasMetricaResponse>> obtenerFiltroEstados() {
        return ResponseEntity.ok(ApiResponse.ok(incidenciaService.obtenerFiltroEstados()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DTOIncidenciaGestionListado>>> listarIncidencias(
            @ModelAttribute DTOIncidenciaFiltro filtro,
            @PageableDefault(sort = "fechaHoraInicio", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DTOIncidenciaGestionListado> paginaResultado = incidenciaService.obtenerIncidencias(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.ok(paginaResultado));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOFormGestionarIncidencia>> obtenerFormularioGestionarIncidencia(@PathVariable UUID id) {
        DTOFormGestionarIncidencia dto = incidenciaService.obtenerformularioGestionarIncidencia(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOGestionIncidenciaResponse>> gestionarIncidencia(
            @PathVariable UUID id,
            @Valid @RequestBody DTOGestionIncidenciaRequest dto) {
        DTOGestionIncidenciaResponse response = incidenciaService.gestionarIncidencia(id, dto);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

}
