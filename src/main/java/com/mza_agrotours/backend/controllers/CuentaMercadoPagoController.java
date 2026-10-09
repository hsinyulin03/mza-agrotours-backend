package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.establecimiento.DTOCuentaMercadoPagoEstado;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Vinculación de la cuenta de Mercado Pago del establecimiento. Solo para el productor líder (titular).
 */
@RestController
@RequestMapping("/establecimientos/{establecimientoId}/mercadopago")
public class CuentaMercadoPagoController {

    private final CuentaMercadoPagoService cuentaMercadoPagoService;

    public CuentaMercadoPagoController(CuentaMercadoPagoService cuentaMercadoPagoService) {
        this.cuentaMercadoPagoService = cuentaMercadoPagoService;
    }

    @GetMapping
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<DTOCuentaMercadoPagoEstado>> obtenerEstado(@PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(cuentaMercadoPagoService.obtenerEstado(establecimientoId)));
    }

    /**
     * Devuelve la URL de Mercado Pago a la que el front tiene que redirigir al productor para autorizar.
     */
    @GetMapping("/vincular")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<String>> obtenerUrlVinculacion(@PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(cuentaMercadoPagoService.generarUrlVinculacion(establecimientoId)));
    }
}