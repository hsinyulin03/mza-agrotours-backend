package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateResp;
import com.mza_agrotours.backend.services.SolicitudEstablecimientoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/solicitudes-establecimiento")
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

}
