package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.PaisGetDTO;
import com.mza_agrotours.backend.services.PaisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/pais")
public class PaisController {
    private final PaisService paisService;

    public PaisController(PaisService paisService) {
        this.paisService = paisService;
    }


    @GetMapping("/")
    public ResponseEntity<?> getPaises() {
        ApiResponse<List<PaisGetDTO>> response = ApiResponse.ok(this.paisService.getPaisesDTO());
        return ResponseEntity.ok(response);
    }
}
