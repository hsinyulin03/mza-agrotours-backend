package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.archivo.DTOFotosResponse;
import com.mza_agrotours.backend.services.ManualUsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manual-usuario")
public class ManualUsuarioController {
    private final ManualUsuarioService manualUsuarioService;

    public ManualUsuarioController(ManualUsuarioService manualUsuarioService) {
        this.manualUsuarioService = manualUsuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DTOFotosResponse>> obtenerManual() {
        return ResponseEntity.ok(ApiResponse.ok(this.manualUsuarioService.obtenerManual()));
    }
}
