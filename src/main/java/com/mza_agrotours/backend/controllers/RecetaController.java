package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.receta.DTORecetaAM;
import com.mza_agrotours.backend.dtos.receta.DTORecetaAMResponse;
import com.mza_agrotours.backend.services.RecetaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
