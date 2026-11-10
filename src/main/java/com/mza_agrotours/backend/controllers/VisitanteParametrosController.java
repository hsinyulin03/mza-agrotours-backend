package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.services.ParametrosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/visitantes/parametros")
public class VisitanteParametrosController {
    private final ParametrosService parametrosService;

    public VisitanteParametrosController(ParametrosService parametrosService) {
        this.parametrosService = parametrosService;
    }

    @GetMapping("/porcentajeComisionSistema")
    public ResponseEntity<ApiResponse<BigDecimal>> obtenerPorcentajeComisionSistema() {
        return ResponseEntity.ok(ApiResponse.ok(parametrosService.getParametros().porcentajeComision()));
    }
}
