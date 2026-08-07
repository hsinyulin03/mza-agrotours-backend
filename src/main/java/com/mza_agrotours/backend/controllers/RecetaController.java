package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.receta.DTORecetaAM;
import com.mza_agrotours.backend.dtos.receta.DTORecetaAMResponse;
import com.mza_agrotours.backend.dtos.receta.DTORecetaDetalleM;
import com.mza_agrotours.backend.dtos.receta.DTORectaBResponse;
import com.mza_agrotours.backend.services.RecetaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/recetas")
public class RecetaController {
    @Autowired
    private RecetaService recetaService;

    @PostMapping("/alta")
    public ResponseEntity<ApiResponse<DTORecetaAMResponse>> altaReceta(
            @Valid @RequestBody DTORecetaAM dto) {
        DTORecetaAMResponse resultado = recetaService.altaReceta(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(resultado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTORecetaDetalleM>> obtenerDatosReceta(
            @PathVariable UUID id) {
        DTORecetaDetalleM resultado = recetaService.obtenerDatosReceta(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));


    }
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DTORecetaAMResponse>> modificarReceta(
            @PathVariable UUID id,
            @Valid @RequestBody DTORecetaAM dto) {
        DTORecetaAMResponse resultado = recetaService.modificarReceta(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DTORectaBResponse>> bajaReceta(
            @PathVariable UUID id) {
        DTORectaBResponse resultado = recetaService.bajaReceta(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
}
