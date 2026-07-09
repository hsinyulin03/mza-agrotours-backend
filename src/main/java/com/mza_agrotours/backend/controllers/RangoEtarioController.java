package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.DTORangoEtarioAlta;
import com.mza_agrotours.backend.entities.RangoEtario;
import com.mza_agrotours.backend.services.RangoEtarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "")
@RequestMapping("/rango_etario")
public class RangoEtarioController extends BaseEntityControllerImpl<RangoEtario, RangoEtarioService>{
    @Autowired
    private RangoEtarioService rangoEtarioService;

    @PostMapping("/alta")
    public ResponseEntity<?> crearRangoEtario(@Valid @RequestBody DTORangoEtarioAlta dto) {
        try {
            rangoEtarioService.crearRangoEtario(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\" Rango Etario creado correctamente }\"");
    } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
    }
}

@DeleteMapping("dar_de_baja/{id}")
public ResponseEntity<?> darDeBaja(@PathVariable Long id) {
    try {

        rangoEtarioService.delete(id);
        return ResponseEntity.status(HttpStatus.CREATED).body("{\"mensaje\":\"Rango Etario " + id + " dado de baja correctamente\"}");

    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
    }
}

}