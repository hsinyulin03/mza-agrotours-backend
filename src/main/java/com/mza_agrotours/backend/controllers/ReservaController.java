package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.services.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reserva")
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getReserva(
            @PathVariable UUID uuid
            // TODO @AuthenticationPrincipal para obtener al usuario
    ) {
        // TODO enviar también el usuario para ver si puede ver esa reserva
        ConsultarReservaDTO dto = service.getConsultarReserva(uuid);
        ApiResponse<ConsultarReservaDTO> response = ApiResponse.ok(dto);
        return ResponseEntity.ok(response);
    }
}