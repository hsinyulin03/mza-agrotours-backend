package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.productor.*;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.services.ProductorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}/productores")
public class ProductorController {
    private final ProductorService productorService;

    public ProductorController(ProductorService productorService) {
        this.productorService = productorService;
    }

    @GetMapping
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).LEER_PRODUCTOR)")
    public ResponseEntity<ApiResponse<List<ProductorGetDTO>>> getAllProductoresVigentes(
            @PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(
                this.productorService.findProductoresVigentes(establecimientoId)));
    }

    @PostMapping
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_PRODUCTOR)")
    public ResponseEntity<ApiResponse<ProductorGetDTO>> crearProductor(
            @PathVariable UUID establecimientoId,
            @Valid @RequestBody ProductorCreateReq productorCreateReq) {
        return ResponseEntity.ok(ApiResponse.ok(
                this.productorService.crearProductor(establecimientoId, productorCreateReq)));
    }

    @PutMapping("/{productorId}")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_PRODUCTOR)")
    public ResponseEntity<ApiResponse<ProductorGetDTO>> modificarProductor(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID productorId,
            @Valid @RequestBody ProductorUpdateReq productorUpdateReq,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        return ResponseEntity.ok(ApiResponse.ok(this.productorService.modificarRolProductor(
                establecimientoId, productorId, productorUpdateReq, usuarioAuthDetails.getEmail())));
    }

    @DeleteMapping("/{productorId}")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_PRODUCTOR)")
    public ResponseEntity<ApiResponse<Boolean>> eliminarProductor(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID productorId,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        boolean fueEliminado = this.productorService.bajaProductor(
                establecimientoId, productorId, usuarioAuthDetails.getEmail());
        // Si la baja falla, el servicio lanza AppException y nunca llegamos aca
        return ResponseEntity.ok(ApiResponse.ok(fueEliminado));
    }

    @PostMapping("/{productorId}/suspension")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_PRODUCTOR)")
    public ResponseEntity<ApiResponse<ProductorGetDTO>> suspenderProductor(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID productorId,
            @Valid @RequestBody ProductorSuspenderReq productorSuspenderReq,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        return ResponseEntity.ok(ApiResponse.ok(this.productorService.suspenderProductor(
                establecimientoId,
                productorId,
                productorSuspenderReq.getMotivo(),
                productorSuspenderReq.getFechaHoraFinPrevista(),
                usuarioAuthDetails.getEmail())));
    }

    @DeleteMapping("/{productorId}/suspension")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_PRODUCTOR)")
    public ResponseEntity<ApiResponse<ProductorGetDTO>> levantarSuspension(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID productorId,
            @Valid @RequestBody ProductorLevantarSuspensionReq productorLevantarSuspensionReq,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails) {
        return ResponseEntity.ok(ApiResponse.ok(this.productorService.levantarSuspension(
                establecimientoId,
                productorId,
                productorLevantarSuspensionReq.getMotivo(),
                usuarioAuthDetails.getEmail())));
    }

    @GetMapping("/roles")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_PRODUCTOR)")
    public ResponseEntity<ApiResponse<List<RolGetShortDTO>>> obtenerRolesAdmin(@PathVariable UUID establecimientoId) {
        return ResponseEntity.ok(ApiResponse.ok(this.productorService.obtenerRolesProductor(establecimientoId)));
    }
}
