package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.services.roles_permisos.RolService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RolController {
    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<RolGetCatalogoDTO>>> obtenerRolesAdminCatalogo() {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                                .obtenerRolesAdminCatalogo()));
    }

    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<RolCreateResponse>> crearRolAdmin(
            @Valid
            @RequestBody RolCreateRequest rolCreateRequest) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .crearRolAdmin(rolCreateRequest)));
    }

    @PutMapping("/admin/{rolId}")
    public ResponseEntity<ApiResponse<RolUpdateResponse>> modificarRolAdmin(
            @PathVariable String rolId,
            @Valid
            @RequestBody RolUpdateRequest rolUpdateRequest) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .modificarRolAdmin(rolId, rolUpdateRequest)));
    }

    @DeleteMapping("/admin/{rolId}")
    public ResponseEntity<ApiResponse<Boolean>> bajaRolAdmin(
            @PathVariable String rolId) {
        return ResponseEntity.ok(ApiResponse.ok(this.rolService
                .bajaRolAdmin(rolId)));
    }
}
