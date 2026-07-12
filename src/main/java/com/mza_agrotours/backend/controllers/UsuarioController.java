package com.mza_agrotours.backend.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioCreateReq;
import com.mza_agrotours.backend.dtos.UsuarioGetDTO;
import com.mza_agrotours.backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody UsuarioCreateReq usuarioCreateReq) throws Exception {
        UsuarioGetDTO usuarioGetDTO = this.usuarioService.createUsuario(usuarioCreateReq);
        ApiResponse<UsuarioGetDTO> response = ApiResponse.ok(usuarioGetDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUsuarioMeByEmail(@RequestHeader("Authorization") String authorizationHeader) throws Exception {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new Exception("Invalid token format. Expected 'Bearer <token>'");
        }

        String token = authorizationHeader.substring(7);
        String email = FirebaseAuth.getInstance().verifyIdToken(token).getEmail();
        UsuarioGetDTO usuarioGetDTO = this.usuarioService.getUsuarioByEmail(email);
        ApiResponse<UsuarioGetDTO> response = ApiResponse.ok(usuarioGetDTO);

        return ResponseEntity.ok(response);
    }
}
