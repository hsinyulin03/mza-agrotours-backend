package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasCreateReq;
import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasGetDTO;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.services.AdministradorSistemasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public AdminSistemasGetDTO createAdmin(@RequestBody AdminSistemasCreateReq adminSistemasCreateReq) {
        return this.administradorSistemasService.createAdmin(adminSistemasCreateReq);
    }

    @GetMapping("/roles")
    public List<RolGetShortDTO> obtenerRolesAdmin() {
        return this.administradorSistemasService.obtenerRolesAdmin();
    }
}
