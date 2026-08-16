package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.roles_permisos.GrupoPermisoDTO;
import com.mza_agrotours.backend.services.PermisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permisos")
public class PermisoController {
    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @GetMapping("/grupos-permisos/admin")
    public ResponseEntity<ApiResponse<List<GrupoPermisoDTO>>> obtenerPermisosAdmin() {
        return ResponseEntity.ok(ApiResponse.ok(this.permisoService
                .obtenerPermisosAdmin()));
    }
}
