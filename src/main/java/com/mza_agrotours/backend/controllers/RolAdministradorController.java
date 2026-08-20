package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.services.roles_permisos.RolService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
public class RolAdministradorController {
    private final RolService rolService;

    public RolAdministradorController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RolGetCatalogoDTO>>> obtenerRolesAdminCatalogo() {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                                .obtenerRolesAdminCatalogo()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RolCreateResponse>> crearRolAdmin(
            @Valid
            @RequestBody RolCreateRequest rolCreateRequest) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .crearRolAdmin(rolCreateRequest)));
    }

    @PutMapping("/{rolId}")
    public ResponseEntity<ApiResponse<RolUpdateResponse>> modificarRolAdmin(
            @PathVariable String rolId,
            @Valid
            @RequestBody RolUpdateRequest rolUpdateRequest) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .modificarRolAdmin(rolId, rolUpdateRequest)));
    }

    @DeleteMapping("/{rolId}")
    public ResponseEntity<ApiResponse<Boolean>> bajaRolAdmin(
            @PathVariable String rolId) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .bajaRolAdmin(rolId)));
    }
}
