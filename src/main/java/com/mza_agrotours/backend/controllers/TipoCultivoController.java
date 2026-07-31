package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOCatalogoTipoCultivo;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOEstacionalidad;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoAM;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoEditarDetalle;
import com.mza_agrotours.backend.services.TipoCultivoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tipos-cultivo")
public class TipoCultivoController {

    @Autowired
    private TipoCultivoService tipoCultivoService;

    @PostMapping("/alta")
    public ResponseEntity<ApiResponse<DTOTipoCultivoEditarDetalle>> altaTipoCultivo(
            @Valid @RequestBody DTOTipoCultivoAM dto) {
        DTOTipoCultivoEditarDetalle resultado = tipoCultivoService.altaTipoCultivo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(resultado));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOTipoCultivoEditarDetalle>> obtenerDatosTipoCultivo(
            @PathVariable UUID id) {
        DTOTipoCultivoEditarDetalle resultado = tipoCultivoService.obtenerDatosTipoCultivo(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOTipoCultivoEditarDetalle>> modificarTipoCultivo(
            @PathVariable UUID id,
            @Valid @RequestBody DTOTipoCultivoAM dto) {
        DTOTipoCultivoEditarDetalle resultado = tipoCultivoService.modificarTipoCultivo(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }

    @GetMapping("/estacionalidades")
    public ResponseEntity<ApiResponse<List<DTOEstacionalidad>>> consultarEstacionalidades() {
        List<DTOEstacionalidad> resultado = tipoCultivoService.consultarEstacionalidades();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<DTOCatalogoTipoCultivo>> consultarCatalogoTipoCultivo() {
        DTOCatalogoTipoCultivo resultado = tipoCultivoService.consultarCatalogoTipoCultivo();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(resultado));
    }

}