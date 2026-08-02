package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDTO;
import com.mza_agrotours.backend.services.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reserva")
@Validated
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<ApiResponse<ConsultarReservaDTO>> getReserva(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        ConsultarReservaDTO dto = service.getConsultarReserva(uuid,email);
        ApiResponse<ConsultarReservaDTO> response = ApiResponse.ok(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservar") // TODO path
    public ResponseEntity<ApiResponse<ConsultarReservaDTO>> iniciarReserva(
            @Valid @RequestBody RealizarReservaDTO dtoEntrada,
            @AuthenticationPrincipal String firebaseUID // TODO UsuarioAuthDetails
    ) {
        ConsultarReservaDTO dtoSalida = service.handleIniciarReserva(dtoEntrada, firebaseUID);
        ApiResponse<ConsultarReservaDTO> response = ApiResponse.ok(dtoSalida);
        return ResponseEntity.ok(response);
    }
}