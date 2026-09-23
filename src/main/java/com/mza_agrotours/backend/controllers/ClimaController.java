package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.clima.ClimaPronosticoGetResponse;
import com.mza_agrotours.backend.services.ClimaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clima")
public class ClimaController {
    private final ClimaService climaService;

    public ClimaController(ClimaService climaService) {
        this.climaService = climaService;
    }

    @GetMapping("/{departamentoNombre}")
    public ResponseEntity<ApiResponse<ClimaPronosticoGetResponse>> obtenerPronosticoPorDepartamento(@PathVariable String departamentoNombre) {
        return ResponseEntity.ok(ApiResponse.ok(this.climaService.obtenerPronosticoByDepartamentoNombre(departamentoNombre)));
    }
}
