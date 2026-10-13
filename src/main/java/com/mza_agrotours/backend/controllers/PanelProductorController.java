package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.PanelProductor.DTOResumenPanelProductor;
import com.mza_agrotours.backend.services.PanelProductorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}/panel")
public class PanelProductorController {
    @Autowired
    private PanelProductorService service;

    @GetMapping("/resumen")
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<DTOResumenPanelProductor>> obtenerResumen(@PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(this.service.obtenerResumen(establecimientoId)));
    }
}
