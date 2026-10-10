package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.reservas.*;
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
            @PathVariable String uuid,
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
    public ResponseEntity<ApiResponse<IniciarReservaDTO>> iniciarReserva(
            @Valid @RequestBody RealizarReservaDTO dtoEntrada,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        IniciarReservaDTO dtoSalida = service.handleIniciarReserva(dtoEntrada, email);
        ApiResponse<IniciarReservaDTO> response = ApiResponse.ok(dtoSalida);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancelarPago/{preferenceId}")
    public ResponseEntity<ApiResponse<?>> cancelarPago(
            @PathVariable String preferenceId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        service.handleCancelarPago(preferenceId, email);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/cancelarReservaCondicion/{reservaId}")
    public ResponseEntity<ApiResponse<IniciarReembolsoDTO>> cancelarReservaCondicion(
            @PathVariable String reservaId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        IniciarReembolsoDTO dtoSalida = service.handleCancelarReservaCondicion(reservaId, email);
        ApiResponse<IniciarReembolsoDTO> response = ApiResponse.ok(dtoSalida);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancelarReserva/{reservaId}")
    public ResponseEntity<ApiResponse<IniciarReembolsoDTO>> cancelarReserva(
            @PathVariable String reservaId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        IniciarReembolsoDTO dtoSalida = service.handleCancelarReserva(reservaId, email);
        ApiResponse<IniciarReembolsoDTO> response = ApiResponse.ok(dtoSalida);
        return ResponseEntity.ok(response);
    }
}