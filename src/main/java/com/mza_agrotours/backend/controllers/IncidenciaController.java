package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.incidencia.DTOListadoIncidenciaVisitante;
import com.mza_agrotours.backend.dtos.incidencia.IncidenciaCreateResponse;
import com.mza_agrotours.backend.dtos.incidencia.IncidenciaCreateRequest;
import com.mza_agrotours.backend.services.IncidenciaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DTOListadoIncidenciaVisitante>>> getIncidenciasDeVisitante(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
            @PageableDefault(size = 10, sort = "fechaHoraInicio", direction = Sort.Direction.ASC) Pageable pageable) {

        String email = usuarioAuthDetails.getEmail();
        Page<DTOListadoIncidenciaVisitante> incidencias =
                incidenciaService.listarIncidenciasDeVisitante(email, pageable);
        return ResponseEntity.ok(ApiResponse.ok(incidencias));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<IncidenciaCreateResponse>> createIncidencia(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
            @Valid @RequestBody IncidenciaCreateRequest request) {

        String email = usuarioAuthDetails.getEmail();
        IncidenciaCreateResponse incidencia = incidenciaService.crearIncidencia(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(incidencia));
    }
}
