package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasCreateReq;
import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasGetDTO;
import com.mza_agrotours.backend.dtos.administrador_sistemas.AdministradorSistemasUpdateReq;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.services.AdministradorSistemasService;
import jakarta.validation.Valid;
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
    public List<AdminSistemasGetDTO> findAllAdminSistemasVigentes() {
        return this.administradorSistemasService.findAllAdminSistemasVigentes();
    }

    @PostMapping("/create")
    public AdminSistemasGetDTO createAdmin(
            @Valid
            @RequestBody AdminSistemasCreateReq adminSistemasCreateReq) {
        return this.administradorSistemasService.createAdmin(adminSistemasCreateReq);
    }

    @PutMapping("/update/{adminId}")
    public AdminSistemasGetDTO updateRolAdmin(@PathVariable UUID adminId,
                                              @Valid
                                              @RequestBody AdministradorSistemasUpdateReq administradorSistemasUpdateReq) {
        return this.administradorSistemasService.updateRolAdmin(adminId, administradorSistemasUpdateReq);
    }

    @GetMapping("/roles")
    public List<RolGetShortDTO> obtenerRolesAdmin() {
        return this.administradorSistemasService.obtenerRolesAdmin();
    }
}
