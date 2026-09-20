package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.archivo.PresignArchivosRequest;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.services.ArchivoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cualquier usuario autenticado puede subir pruebas: la solicitud existe
 * justamente porque todavia no tiene un establecimiento sobre el cual pedir
 * permisos.
 */
@RestController
@RequestMapping("/solicitudes-establecimiento/archivos")
@Validated
public class ArchivoSolicitudEstablecimientoController {
    private final ArchivoService archivoService;

    public ArchivoSolicitudEstablecimientoController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<List<ArchivoUploadResponse>>> presign(
            @Valid @RequestBody PresignArchivosRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(this.archivoService
                .getSignedArchivos(request.getArchivos(), CarpetaArchivo.SOLICITUDES_ESTABLECIMIENTO)));
    }
}
