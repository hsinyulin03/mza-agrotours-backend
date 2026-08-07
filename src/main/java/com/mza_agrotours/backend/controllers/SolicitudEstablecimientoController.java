package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.ObservacionSolicitudDTO;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateResp;
import com.mza_agrotours.backend.services.SolicitudEstablecimientoService;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solicitudes-establecimiento")
@Validated
public class SolicitudEstablecimientoController {
    private final SolicitudEstablecimientoService solicitudEstablecimientoService;

    public SolicitudEstablecimientoController(SolicitudEstablecimientoService solicitudEstablecimientoService) {
        this.solicitudEstablecimientoService = solicitudEstablecimientoService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> crearSolicitudEstablecimiento(
            @Valid @RequestBody SolicitudEstablecimientoCreateReq solicitudEstablecimientoCreateReq,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails)
            throws Exception {
        String emailUsuario = usuarioAuthDetails.getEmail();
        SolicitudEstablecimientoCreateResp nuevaSolicitudEstablecimiento = solicitudEstablecimientoService.crearSolicitudEstablecimiento(solicitudEstablecimientoCreateReq, emailUsuario);
        //TODO: Notificar al usuario que se ha creado la solicitud
        return ResponseEntity.ok().body(ApiResponse.ok(nuevaSolicitudEstablecimiento));
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerSolicitudesPorUsuario(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        String emailUsuario = usuarioAuthDetails.getEmail();
        return ResponseEntity.ok().body(ApiResponse.ok(solicitudEstablecimientoService.obtenerSolicitudesPorUsuario(emailUsuario)));
    }

    @GetMapping("/me/{solicitudId}")
    public ResponseEntity<?> obtenerSolicitudDetallePorUsuario(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
                                                               @UUID @PathVariable String solicitudId) {
        String emailUsuario = usuarioAuthDetails.getEmail();
        return ResponseEntity.ok().body(ApiResponse.ok(solicitudEstablecimientoService.obtenerSolicitudPorUsuario(emailUsuario, solicitudId)));
    }

    @PostMapping("/observar/{solicitudId}")
    public ResponseEntity<?> observarSolicitud(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails,
                                               @UUID @PathVariable String solicitudId,
                                               @RequestBody ObservacionSolicitudDTO observacionSolicitudDTO) {
        String emailObservador = usuarioAuthDetails.getEmail();
        return ResponseEntity.ok().body(ApiResponse.ok(solicitudEstablecimientoService.observarSolicitud(emailObservador, solicitudId, observacionSolicitudDTO)));
    }
}
