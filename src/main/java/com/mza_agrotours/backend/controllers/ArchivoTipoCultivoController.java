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
 * El admin sube la imagen al bucket antes de crear o editar el cultivo.
 * La ruta cae bajo /admin/tipos-cultivo, que SecurityConfig ya exige
 * GESTIONAR_CULTIVOS para todo lo que no sea un GET.
 */
@RestController
@RequestMapping("/admin/tipos-cultivo/archivos")
@Validated
public class ArchivoTipoCultivoController {
    private final ArchivoService archivoService;

    public ArchivoTipoCultivoController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<List<ArchivoUploadResponse>>> presign(
            @Valid @RequestBody PresignArchivosRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                this.archivoService.getSignedArchivos(request.getArchivos(), CarpetaArchivo.CULTIVOS)));
    }
}
