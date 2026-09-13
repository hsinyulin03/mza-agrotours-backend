package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOFiltroTemporadaCultivo;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoDetalleVisitante;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoVisitante;
import com.mza_agrotours.backend.services.TipoCultivoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tipo-cultivo")
public class TipoCultivoVisitanteController {
    @Autowired
    private TipoCultivoService tipoCultivoService;
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DTOTipoCultivoVisitante>>> consultarCultivosVisitantes(
            @RequestParam(required = false) Boolean enTemporada,
            Pageable pageable) {
        Page<DTOTipoCultivoVisitante> resultado = tipoCultivoService.consultarCultivosVisitantes(enTemporada, pageable);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    @GetMapping("/filtros/temporada")
    public ResponseEntity<ApiResponse<DTOFiltroTemporadaCultivo>> obtenerFiltroTemporada() {
        return ResponseEntity.ok(ApiResponse.ok(tipoCultivoService.obtenerFiltroTemporada()));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOTipoCultivoDetalleVisitante>> obtenerDetalleCultivoVisitante(
            @PathVariable UUID id) {
        DTOTipoCultivoDetalleVisitante resultado = tipoCultivoService.obtenerDetalleCultivoVisitante(id);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
    }

