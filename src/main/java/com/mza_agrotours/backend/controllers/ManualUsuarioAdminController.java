package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.DTOFotosResponse;
import com.mza_agrotours.backend.services.ManualUsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/manual-usuario")
public class ManualUsuarioAdminController {
    private final ManualUsuarioService manualUsuarioService;

    public ManualUsuarioAdminController(ManualUsuarioService manualUsuarioService) {
        this.manualUsuarioService = manualUsuarioService;
    }

    @PutMapping
    public ResponseEntity<ApiResponse<DTOFotosResponse>> actualizarManual(
            @Valid @RequestBody ArchivoClaimRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(this.manualUsuarioService.actualizarManual(request)));
    }
}
