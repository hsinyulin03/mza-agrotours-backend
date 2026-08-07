package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasGetDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdministradorSistemasMapper {

    default List<AdminSistemasGetDTO> administradorSistemasListToAdminSistemasGetDTOList(List<AdministradorSistemas> administradorSistemas) {
        return administradorSistemas.stream().map(this::administradorSistemasToAdminSistemasGetDTO).toList();
    }

    default AdminSistemasGetDTO administradorSistemasToAdminSistemasGetDTO(AdministradorSistemas administradorSistemas) {
        AdminSistemasGetDTO adminSistemasGetDTO = new AdminSistemasGetDTO();
        adminSistemasGetDTO.setId(administradorSistemas.getId().toString());

        //Data usuario
        adminSistemasGetDTO.setNombreUsuario(administradorSistemas.getUsuario().getNombre());
        adminSistemasGetDTO.setEmailUsuario(administradorSistemas.getUsuario().getEmail());
        adminSistemasGetDTO.setIdentificacion(administradorSistemas.getUsuario().getIdentificacion());

        //Data rol
        adminSistemasGetDTO.setNombreRol(administradorSistemas.getRol().getNombre());
        adminSistemasGetDTO.setEsLider(administradorSistemas.getRol().getNombre().equals("Administrador Líder"));

        return adminSistemasGetDTO;
    }
}
