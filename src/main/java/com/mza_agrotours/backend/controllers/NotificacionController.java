package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.enums.ScopeNotificacionNombre;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/notificacion")
@Validated
public class NotificacionController {
    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificacionDTO>>> listarNotificaciones(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
            @PageableDefault(page = 0, size = 10, sort = "fechaHoraAlta", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<NotificacionDTO> dtos = service.listarNotificaciones(usuarioAuthDetails.getEmail(), ScopeNotificacionNombre.VISITANTE, null, pageable);
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/no-leidas/cantidad")
    public ResponseEntity<?> contarNoLeidas(
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        long cantidad = service.contarNoLeidas(usuarioAuthDetails.getEmail(), ScopeNotificacionNombre.VISITANTE, null);
        return ResponseEntity.ok(ApiResponse.ok(cantidad));
    }

    @PatchMapping("/{idNotificacion}/leer")
    public ResponseEntity<?> marcarLeida(
            @PathVariable UUID idNotificacion,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        NotificacionDTO notificacionActualizada= service.marcarLeida(idNotificacion, usuarioAuthDetails.getEmail(), ScopeNotificacionNombre.VISITANTE, null);
        return ResponseEntity.ok(ApiResponse.ok(notificacionActualizada));
    }

    @PatchMapping("/leerHasta/{fechaHoraHasta}")
    public ResponseEntity<ApiResponse<Integer>> marcarLeidasListado(
            @Valid @PastOrPresent @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHoraHasta,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        return ResponseEntity.ok(ApiResponse.ok(service.marcarLeidasListado(fechaHoraHasta, usuarioAuthDetails.getEmail(), ScopeNotificacionNombre.VISITANTE, null)));
    }

}
