package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.receta.DTOFiltroCultivoReceta;
import com.mza_agrotours.backend.dtos.receta.DTOFiltroDificultadReceta;
import com.mza_agrotours.backend.dtos.receta.DTOFiltroDuracionReceta;
import com.mza_agrotours.backend.dtos.receta.DTORecetaCatalogoVisitante;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOFiltroTemporadaCultivo;
import com.mza_agrotours.backend.enums.Dificultad;
import com.mza_agrotours.backend.enums.DuracionNombre;
import com.mza_agrotours.backend.services.RecetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recetas")
public class RecetaVisitanteController {
    @Autowired
    private RecetaService recetaService;

    @GetMapping("/filtros/dificultad")
    public ResponseEntity<ApiResponse<List<DTOFiltroDificultadReceta>>> obtenerFiltroDificultad() {
        return ResponseEntity.ok(ApiResponse.ok(recetaService.obtenerFiltroDificultad()));
    }

    @GetMapping("/filtros/duracion")
    public ResponseEntity<ApiResponse<List<DTOFiltroDuracionReceta>>> obtenerFiltroDuracion() {
        return ResponseEntity.ok(ApiResponse.ok(recetaService.obtenerFiltroDuracion()));
    }

    @GetMapping("/filtros/cultivos")
    public ResponseEntity<ApiResponse<List<DTOFiltroCultivoReceta>>> obtenerFiltroCultivos() {
        return ResponseEntity.ok(ApiResponse.ok(recetaService.obtenerFiltroCultivos()));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DTORecetaCatalogoVisitante>>> consultarRecetasVisitante(
            @RequestParam(required = false) Dificultad dificultad,
            @RequestParam(required = false) DuracionNombre duracion,
            @RequestParam(required = false) UUID cultivoId,
            @PageableDefault(size = 9, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DTORecetaCatalogoVisitante> resultado = recetaService.consultarCatalogoVisitante(cultivoId, dificultad, duracion, pageable);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
}
