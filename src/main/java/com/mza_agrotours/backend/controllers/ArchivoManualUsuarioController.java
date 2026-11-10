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
 * El admin sube el pdf al bucket antes de confirmarlo como manual vigente.
 * La ruta cae bajo /admin/manual-usuario, que SecurityConfig exige
 * GESTIONAR_PARAMETROS.
 */
@RestController
@RequestMapping("/admin/manual-usuario/archivos")
@Validated
public class ArchivoManualUsuarioController {
    private final ArchivoService archivoService;

    public ArchivoManualUsuarioController(ArchivoService archivoService) {
        this.archivoService = archivoService;
    }

    @PostMapping("/presign")
    public ResponseEntity<ApiResponse<List<ArchivoUploadResponse>>> presign(
            @Valid @RequestBody PresignArchivosRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                this.archivoService.getSignedArchivos(request.getArchivos(), CarpetaArchivo.MANUALES)));
    }
}
