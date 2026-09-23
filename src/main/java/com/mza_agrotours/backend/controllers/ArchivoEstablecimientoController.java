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
 * El titular sube la portada al bucket antes de guardar el establecimiento.
 */
@RestController
@RequestMapping("/establecimientos/{establecimientoId}/archivos")
@Validated
public class ArchivoEstablecimientoController {
    private final ArchivoService archivoService;

    public ArchivoEstablecimientoController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping("/presign")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<List<ArchivoUploadResponse>>> presign(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody PresignArchivosRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                this.archivoService.getSignedArchivos(request.getArchivos(), CarpetaArchivo.ESTABLECIMIENTOS)));
    }
}
