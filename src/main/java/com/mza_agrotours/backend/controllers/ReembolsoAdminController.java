package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.pago.DetalleReembolsoAdminDTO;
import com.mza_agrotours.backend.dtos.pago.ListarReembolsoAdminDTO;
import com.mza_agrotours.backend.services.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequestMapping("/admin/reembolsos")
@RestController
public class ReembolsoAdminController {
    private final ReservaService reservaService;

    public ReembolsoAdminController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ListarReembolsoAdminDTO>>> listarReembolsosImpagos() {
        return ResponseEntity.ok(ApiResponse.ok(this.reservaService.listarReembolsosImpagos()));
    }

    @GetMapping("/{idReembolso}")
    public ResponseEntity<ApiResponse<DetalleReembolsoAdminDTO>> getDetalleReembolso(@PathVariable UUID idReembolso) {
        return ResponseEntity.ok(ApiResponse.ok(this.reservaService.getDetalleReembolsoAdmin(idReembolso)));
    }
}
