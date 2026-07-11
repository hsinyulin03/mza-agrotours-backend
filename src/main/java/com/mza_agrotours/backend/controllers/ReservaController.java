package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.services.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/reserva")
@CrossOrigin(origins = "*")
public class ReservaController {
    private final ReservaService service;
    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping("/detalle")
    public ResponseEntity<?> ping(@RequestParam UUID id) {
        try{
            return ResponseEntity.status(200).body(service.getConsultarReserva(id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ocurrió un error, intente más tarde");
        }
    }
}