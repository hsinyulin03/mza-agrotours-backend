package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.ListarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDTO;
import com.mza_agrotours.backend.services.ReservaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reserva")
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping("/get/{uuid}")
    public ResponseEntity<ApiResponse<ConsultarReservaDTO>> getReserva(
            @PathVariable UUID uuid,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        ConsultarReservaDTO dto = service.getConsultarReserva(uuid,email);
        ApiResponse<ConsultarReservaDTO> response = ApiResponse.ok(dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    public ResponseEntity<ApiResponse<List<ListarReservaDTO>>> getReservaList(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        List<ListarReservaDTO> dtos = service.getListarReservas(email);
        ApiResponse<List<ListarReservaDTO>> response = ApiResponse.ok(dtos);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reservar")
    public ResponseEntity<ApiResponse<ConsultarReservaDTO>> iniciarReserva(
            @Valid @RequestBody RealizarReservaDTO dtoEntrada,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        ConsultarReservaDTO dtoSalida = service.handleIniciarReserva(dtoEntrada, email);
        ApiResponse<ConsultarReservaDTO> response = ApiResponse.ok(dtoSalida);
        return ResponseEntity.ok(response);
    }

}