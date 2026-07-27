package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoAM;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoDatos;
import com.mza_agrotours.backend.services.TipoCultivoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tipos-cultivo")
public class TipoCultivoController {

    @Autowired
    private TipoCultivoService tipoCultivoService;

    @PostMapping("/alta")
    public ResponseEntity<ApiResponse<DTOTipoCultivoDatos>> altaTipoCultivo(
            @Valid @RequestBody DTOTipoCultivoAM dto) {
        DTOTipoCultivoDatos resultado = tipoCultivoService.altaTipoCultivo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(resultado));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOTipoCultivoDatos>> obtenerDatosTipoCultivo(
            @PathVariable UUID id) {
        DTOTipoCultivoDatos resultado = tipoCultivoService.obtenerDatosTipoCultivo(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOTipoCultivoDatos>> modificarTipoCultivo(
            @PathVariable UUID id,
            @Valid @RequestBody DTOTipoCultivoAM dto) {
        DTOTipoCultivoDatos resultado = tipoCultivoService.modificarTipoCultivo(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }

}