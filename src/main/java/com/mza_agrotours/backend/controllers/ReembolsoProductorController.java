package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.pago.ReembolsoGetCatalogoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoProductorDTO;
import com.mza_agrotours.backend.services.ReservaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}/reembolsos")
public class ReembolsoProductorController {

    private final ReservaService reservaService;

    public ReembolsoProductorController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    //US-RESE-07: Reembolsos que Mercado Pago no pudo devolver y tiene que hacer el productor
    @GetMapping("/pendientes")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_REEMBOLSOS_PENDIENTES)")
    public ResponseEntity<ApiResponse<Page<ReembolsoGetCatalogoDTO>>> obtenerReembolsosPendientes(
            @PathVariable UUID establecimientoId,
            @PageableDefault(page = 0, size = 10, sort = {"fechaHoraPedido", "id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ReembolsoGetCatalogoDTO> pendientes = reservaService.listarReembolsosPendientes(establecimientoId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(pendientes));
    }

    //US-RESE-07: Reintentar un reembolso pendiente
    @PostMapping("/{reembolsoId}/reembolsar")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_REEMBOLSOS_PENDIENTES)")
    public ResponseEntity<ApiResponse<ResultadoReembolsoProductorDTO>> reembolsar(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID reembolsoId) {
        ResultadoReembolsoProductorDTO resultado = reservaService.reembolsarPorProductor(establecimientoId, reembolsoId);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
}
