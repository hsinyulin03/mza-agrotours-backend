package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.PanelSistema.DTOResumenPanel;
import com.mza_agrotours.backend.services.PanelSistemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/panel")
public class PanelSistemaController {
    @Autowired
    private PanelSistemaService service;
    @GetMapping("/resumen")
    public ResponseEntity<ApiResponse<DTOResumenPanel>> obtenerResumen() {
        return ResponseEntity.ok(ApiResponse.ok(this.service.obtenerResumen()));
    }


}
