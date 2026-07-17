package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.rangoEtario.DTORangoEtarioAlta;
import com.mza_agrotours.backend.dtos.rangoEtario.DTORangoEtarioGet;
import com.mza_agrotours.backend.services.RangoEtarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rango-etarios")

public class RangoEtarioController {
    @Autowired
    private RangoEtarioService rangoEtarioService;

    @PostMapping("/alta")
    public ResponseEntity<?> crearRangoEtario(@Valid @RequestBody DTORangoEtarioAlta dto) throws Exception {

            DTORangoEtarioGet nuevoRango =rangoEtarioService.crearRangoEtario(dto);
            //Está bien devolver un dto?
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(nuevoRango));
    }

    @GetMapping("/all")
    public ResponseEntity<?> listarRangos() throws Exception{
        List<DTORangoEtarioGet> lista = rangoEtarioService.listarRangosActivos();
        return ResponseEntity.ok(ApiResponse.ok(lista));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> darDeBaja(@PathVariable UUID id) throws Exception {
            rangoEtarioService.darDeBaja(id);
            ApiResponse<String> response = ApiResponse.ok("Rango Etario " + id + " dado de baja correctamente");
            return ResponseEntity.ok(response);
    }

}