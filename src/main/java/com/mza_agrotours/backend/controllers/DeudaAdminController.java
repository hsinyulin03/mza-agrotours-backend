package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.deudas.ListarDeudaDTO;
import com.mza_agrotours.backend.services.DeudaEstablecimientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/admin/deudas")
@RestController
public class DeudaAdminController {
    private final DeudaEstablecimientoService deudaEstablecimientoService;

    public DeudaAdminController(DeudaEstablecimientoService deudaEstablecimientoService) {
        this.deudaEstablecimientoService = deudaEstablecimientoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ListarDeudaDTO>>> listarDeudas() {
        return ResponseEntity.ok(ApiResponse.ok(this.deudaEstablecimientoService.listarDeudas()));
    }
}