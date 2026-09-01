package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.administrador_sistemas.*;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.services.AdministradorSistemasService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/administradores-sistemas")
public class AdministradorSistemasController {
    private final AdministradorSistemasService administradorSistemasService;

    public AdministradorSistemasController(AdministradorSistemasService administradorSistemasService) {
        this.administradorSistemasService = administradorSistemasService;
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<AdminSistemasGetDTO>>> findAllAdminSistemasVigentes() {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService.findAllAdminSistemasVigentes()));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AdminSistemasGetDTO>> createAdmin(
            @Valid
            @RequestBody AdminSistemasCreateReq adminSistemasCreateReq) {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService.createAdmin(adminSistemasCreateReq)));
    }

    @PutMapping("/update/{adminId}")
    public ResponseEntity<ApiResponse<AdminSistemasGetDTO>> updateRolAdmin(@PathVariable UUID adminId,
                                                                          @Valid
                                                                          @RequestBody AdministradorSistemasUpdateReq administradorSistemasUpdateReq,
                                                                          @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        String emailAdminEjecutor = usuarioAuthDetails.getEmail();
        return ResponseEntity.ok(ApiResponse.ok(
                this.administradorSistemasService.updateRolAdmin(adminId, administradorSistemasUpdateReq, emailAdminEjecutor)));
    }

    @DeleteMapping("/{adminId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteAdmin(@PathVariable UUID adminId,
                                                            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        String emailAdminEjecutor = usuarioAuthDetails.getEmail();
        boolean fueEliminado = this.administradorSistemasService.deleteAdmin(adminId, emailAdminEjecutor);
        // Si la baja falla, el servicio lanza AppException y nunca llegamos acá
        return ResponseEntity.ok(ApiResponse.ok(fueEliminado));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RolGetShortDTO>>> obtenerRolesAdmin() {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService.obtenerRolesAdmin()));
    }

    @GetMapping("/establecimientos")
    public ResponseEntity<ApiResponse<List<EstablecimientoAdminDTO>>> obtenerEstablecimientos() {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService.obtenerEstablecimientos()));
    }

    @PostMapping("/establecimientos/{establecimientoId}/suspension")
    public ResponseEntity<ApiResponse<EstablecimientoAdminDTO>> suspenderEstablecimiento(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody EstablecimientoSuspenderReq establecimientoSuspenderReq) {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService.suspenderEstablecimiento(establecimientoId, establecimientoSuspenderReq)));
    }

    @DeleteMapping("/establecimientos/{establecimientoId}/suspension")
    public ResponseEntity<ApiResponse<EstablecimientoAdminDTO>> levantarSuspension(@PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(this.administradorSistemasService.reactivarEstablecimiento(establecimientoId)));
    }
}