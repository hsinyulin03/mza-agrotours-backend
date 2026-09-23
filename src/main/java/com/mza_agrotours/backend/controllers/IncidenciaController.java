package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.incidencia.DTOListadoIncidenciaVisitanteResponse;
import com.mza_agrotours.backend.dtos.incidencia.IncidenciaCreateResponse;
import com.mza_agrotours.backend.dtos.incidencia.IncidenciaCreateRequest;
import com.mza_agrotours.backend.services.IncidenciaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidencia")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getIncidenciasDeVisitante(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        String email = usuarioAuthDetails.getEmail();
        List<DTOListadoIncidenciaVisitanteResponse> incidencias = incidenciaService.listarIncidenciasDeVisitante(email);
        return ResponseEntity.ok(ApiResponse.ok(incidencias));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createIncidencia(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
                                              @Valid @RequestBody IncidenciaCreateRequest request) {
        String email = usuarioAuthDetails.getEmail();
        IncidenciaCreateResponse incidencia = incidenciaService.crearIncidencia(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(incidencia));
    }
}
