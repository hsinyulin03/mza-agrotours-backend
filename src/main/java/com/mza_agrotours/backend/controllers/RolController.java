package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetCatalogoDTO;
import com.mza_agrotours.backend.services.RolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
