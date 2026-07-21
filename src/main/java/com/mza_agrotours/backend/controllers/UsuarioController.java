package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.*;
import com.mza_agrotours.backend.exceptions.UserDeleteConditionNotMetException;
import com.mza_agrotours.backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<?> getUsuarioMeByEmail(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) throws Exception {
        String email = usuarioAuthDetails.getEmail();
        UsuarioGetDTO usuarioGetDTO = this.usuarioService.getUsuarioByEmail(email);
        ApiResponse<UsuarioGetDTO> response = ApiResponse.ok(usuarioGetDTO);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<?> putUsuarioMeByEmail(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails, @Valid @RequestBody UsuarioUpdateReq usuarioUpdateReq) throws Exception {
        String email = usuarioAuthDetails.getEmail();
            UsuarioGetDTO usuarioGetDTO = this.usuarioService.updateUsuarioByEmail(email, usuarioUpdateReq);
            return ResponseEntity.ok(ApiResponse.ok(usuarioGetDTO));
    }


    @GetMapping("/me/meets-delete-conditions")
    public ResponseEntity<?> getCondicionesDeleteUsuarioMeByEmail(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) throws Exception {
        String email = usuarioAuthDetails.getEmail();
        List<CondicionDTO> condiciones = this.usuarioService.getCondicionesDeleteUsuario(email);
        if (!condiciones.isEmpty()) {
            throw new UserDeleteConditionNotMetException("No se puede eliminar el usuario", condiciones);
        }

        return ResponseEntity.ok(ApiResponse.ok(condiciones));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUsuarioMeByEmail(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) throws Exception {
        String email = usuarioAuthDetails.getEmail();
        boolean res = this.usuarioService.deleteUsuarioByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }
}
