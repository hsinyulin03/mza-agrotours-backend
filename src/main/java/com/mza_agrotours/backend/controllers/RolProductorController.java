package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.services.RolService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}/roles")
public class RolProductorController {
    private final RolService rolService;

    public RolProductorController(RolService rolService) {
        this.rolService = rolService;
    }
    @GetMapping
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, 'LEER_ROLES_PRODUCTOR')")
    public ResponseEntity<ApiResponse<List<RolGetCatalogoDTO>>> obtenerRolesProductorCatalogo(
            @PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService.obtenerRolesProductorCatalogo(establecimientoId)));
    }

    @PostMapping
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<RolCreateResponse>> crearRolProductor(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody RolCreateRequest rolCreateRequest) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .crearRolProductor(establecimientoId, rolCreateRequest)));
    }

    @PutMapping("/{rolId}")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<RolUpdateResponse>> modificarRolProductor(
            @PathVariable UUID establecimientoId,
            @PathVariable String rolId,
            @Valid @RequestBody RolUpdateRequest rolUpdateRequest) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .modificarRolProductor(establecimientoId, rolId, rolUpdateRequest)));
    }

    @DeleteMapping("/{rolId}")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<Boolean>> bajaRolProductor(
            @PathVariable UUID establecimientoId,
            @PathVariable String rolId) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .bajaRolProductor(establecimientoId, rolId)));
    }

}
