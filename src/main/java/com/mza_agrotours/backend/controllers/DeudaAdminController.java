package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.deudas.CambiarEstadoDeudaDTO;
import com.mza_agrotours.backend.dtos.deudas.DeudaDetalleDTO;
import com.mza_agrotours.backend.dtos.deudas.ListarDeudaDTO;
import com.mza_agrotours.backend.services.DeudaEstablecimientoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<DeudaDetalleDTO>> detalleDeuda(
            @PathVariable UUID uuid) {
        return ResponseEntity.ok(ApiResponse.ok(this.deudaEstablecimientoService.detalleDeuda(uuid)));
    }

    @PostMapping("/{uuid}/cambio-estado")
    public ResponseEntity<ApiResponse<DeudaDetalleDTO>> cambiarEstadoDeuda(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
            @PathVariable UUID uuid,
            @Valid @RequestBody CambiarEstadoDeudaDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok(this.deudaEstablecimientoService.cambiarEstadoDeuda(uuid, dto, usuarioAuthDetails.getEmail())));
    }
}