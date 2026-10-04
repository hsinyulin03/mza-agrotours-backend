package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.calificacion.CalificacionDTO;
import com.mza_agrotours.backend.dtos.calificacion.CalificacionResponseDTO;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.services.CalificacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/calificaciones")
public class CalificacionController {

    private final CalificacionService service;

    public CalificacionController(CalificacionService service) {
        this.service = service;
    }
    //US-RESE-05-Valorar Experiencia a una reserva finalizada
    //NOTA: El encabezado del modal se puede obtener mediante el endpoint /reserva/get de reservaController (ListarReservaDTO)
    @PostMapping("/reserva/{reservaId}")
    public ResponseEntity<ApiResponse<CalificacionResponseDTO>> calificarReserva(
            @PathVariable UUID reservaId,
            @Valid @RequestBody CalificacionDTO dto,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        CalificacionResponseDTO response = service.calificarReserva(reservaId, dto, email);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }


}
