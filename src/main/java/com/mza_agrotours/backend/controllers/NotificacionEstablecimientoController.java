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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}/notificacion")
@Validated
public class NotificacionEstablecimientoController {

    private final NotificacionService service;

    public NotificacionEstablecimientoController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<Page<NotificacionDTO>>> listar(
            @PathVariable UUID establecimientoId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
            @PageableDefault(page = 0, size = 10, sort = "fechaHoraAlta", direction = Sort.Direction.DESC) Pageable pageable
            ) {
        String email = usuarioAuthDetails.getEmail();
        Page<NotificacionDTO> dtos = service.listarNotificaciones(email, ScopeNotificacionNombre.ESTABLECIMIENTO, establecimientoId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/no-leidas/cantidad")
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<?>  contarNoLeidas(
            @PathVariable UUID establecimientoId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        long cantidad = service.contarNoLeidas(email, ScopeNotificacionNombre.ESTABLECIMIENTO, establecimientoId);
        return ResponseEntity.ok(ApiResponse.ok(cantidad));
    }

    @PatchMapping("/{notificacionId}/leer")
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<?>  marcarLeida(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID notificacionId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ) {
        String email = usuarioAuthDetails.getEmail();
        NotificacionDTO notificacionActualizada = service.marcarLeida(notificacionId, email, ScopeNotificacionNombre.ESTABLECIMIENTO, establecimientoId);
        return ResponseEntity.ok(ApiResponse.ok(notificacionActualizada));
    }

    @PatchMapping("/leerHasta/{fechaHoraHasta}")
    @PreAuthorize("@estAuth.esProductorVigente(authentication, #establecimientoId)")
    public ResponseEntity<?> marcarLeidasListado(
            @PathVariable UUID establecimientoId,
            @Valid @PastOrPresent @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHoraHasta,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        return ResponseEntity.ok(ApiResponse.ok(service.marcarLeidasListado(fechaHoraHasta, usuarioAuthDetails.getEmail(), ScopeNotificacionNombre.ESTABLECIMIENTO, establecimientoId)));
    }


}
