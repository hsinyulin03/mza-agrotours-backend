package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.ParametrosDTO;
import com.mza_agrotours.backend.services.ParametrosService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/parametros")
public class ParametrosController {

    private final ParametrosService parametrosService;

    public ParametrosController(ParametrosService parametrosService) {
        this.parametrosService = parametrosService;
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ParametrosDTO>> updateParametros(
            @RequestBody @Valid ParametrosDTO dto
            ) {
        ParametrosDTO params = parametrosService.setParametros(dto);
        return ResponseEntity.ok(ApiResponse.ok(params));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ParametrosDTO>> getParametros() {
        return ResponseEntity.ok(ApiResponse.ok(parametrosService.getParametros()));
    }
}
