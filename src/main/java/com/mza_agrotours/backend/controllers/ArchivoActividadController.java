package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.archivo.PresignArchivosRequest;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.services.ArchivoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * El cliente sube las fotos al bucket antes de crear o editar la actividad.
 */
@RestController
@RequestMapping("/establecimientos/{establecimientoId}/actividades/archivos")
@Validated
public class ArchivoActividadController {
    private final ArchivoService archivoService;

    public ArchivoActividadController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping("/presign")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<List<ArchivoUploadResponse>>> presign(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody PresignArchivosRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                this.archivoService.getSignedArchivos(request.getArchivos(), CarpetaArchivo.ACTIVIDADES)));
    }
}
