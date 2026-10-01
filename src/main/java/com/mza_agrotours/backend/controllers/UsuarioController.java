package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.*;
import com.mza_agrotours.backend.dtos.chat.ChatInfoRequest;
import com.mza_agrotours.backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuario")
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails, @Valid @RequestBody UsuarioCreateReq usuarioCreateReq) throws Exception {

        UsuarioGetDTO usuarioGetDTO = this.usuarioService.createUsuario(usuarioCreateReq, usuarioAuthDetails);
        ApiResponse<UsuarioGetDTO> response = ApiResponse.ok(usuarioGetDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getUsuarioMeByFirebaseUID(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) throws Exception {
        UsuarioGetDTO usuarioGetDTO = this.usuarioService.getUsuarioByFirebaseUID(usuarioAuthDetails.getFirebaseUID());
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
        return ResponseEntity.ok(ApiResponse.ok(condiciones));
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> deleteUsuarioMeByEmail(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) throws Exception {
        String email = usuarioAuthDetails.getEmail();
        boolean res = this.usuarioService.deleteUsuarioByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    // TODO: check, mepa que en su lugar habría que pedirleselo por body
    @GetMapping("/card/{email}")
    public ResponseEntity<?> getCardUsuarioByEmail(@PathVariable String email) {
        UsuarioCardDTO usuarioCardDTO = this.usuarioService.getUsuarioCardByEmail(email);
        return ResponseEntity.ok(ApiResponse.ok(usuarioCardDTO));
    }

    @PostMapping("/chats")
    public ResponseEntity<ApiResponse<Map<String, String>>> getChatsInfo(@AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails, @Valid @RequestBody List<ChatInfoRequest> chatInfoRequests) {
        return ResponseEntity.ok(ApiResponse.ok(this.usuarioService.getNombresChatByActividadIds(chatInfoRequests, usuarioAuthDetails)));
    }
}
