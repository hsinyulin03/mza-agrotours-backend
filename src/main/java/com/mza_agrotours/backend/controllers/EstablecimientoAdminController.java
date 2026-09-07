package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.administrador_sistemas.EstablecimientoAdminDTO;
import com.mza_agrotours.backend.dtos.administrador_sistemas.EstablecimientoSuspenderReq;
import com.mza_agrotours.backend.services.AdministradorSistemasService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequestMapping("/admin/establecimientos")
@RestController
public class EstablecimientoAdminController {
    private final AdministradorSistemasService administradorSistemasService;

    public EstablecimientoAdminController(AdministradorSistemasService administradorSistemasService) {
        this.administradorSistemasService = administradorSistemasService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EstablecimientoAdminDTO>>> obtenerEstablecimientos() {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService.obtenerEstablecimientos()));
    }

    @PostMapping("/{establecimientoId}/suspension")
    public ResponseEntity<ApiResponse<EstablecimientoAdminDTO>> suspenderEstablecimiento(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody EstablecimientoSuspenderReq establecimientoSuspenderReq,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService
                .suspenderEstablecimiento(establecimientoId, establecimientoSuspenderReq, usuarioAuthDetails.getEmail())));
    }

    @DeleteMapping("/{establecimientoId}/suspension")
    public ResponseEntity<ApiResponse<EstablecimientoAdminDTO>> reactivarEstablecimiento(
            @PathVariable UUID establecimientoId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService
                .reactivarEstablecimiento(establecimientoId, usuarioAuthDetails.getEmail())));
    }
}
