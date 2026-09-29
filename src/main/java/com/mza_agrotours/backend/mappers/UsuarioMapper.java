package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.UsuarioCardDTO;
import com.mza_agrotours.backend.dtos.UsuarioCreateReq;
import com.mza_agrotours.backend.dtos.UsuarioGetDTO;
import com.mza_agrotours.backend.dtos.UsuarioUpdateReq;
import com.mza_agrotours.backend.dtos.acceso.AccesoDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    /**
     * Mapea un DTO de usuario a un usuario.
     * @param usuarioCreateReq
     * @return Usuario sin tipoIdentificacion mapeado.
     */
    @Mapping(target = "tipoIdentificacion", ignore = true)
    Usuario usuarioCreateReqToUsuario(UsuarioCreateReq usuarioCreateReq);

    /**
     * Mapea un usuario a un DTO de usuario.
     * Obtiene tipoIdentificacion a partir de tipoIdentificacion.nombre.
     * @param usuario
     * @return UsuarioGetDTO sin campos paisIso2 y accesos mapeados.
     */
    @Mapping(target = "tipoIdentificacion", source = "usuario.tipoIdentificacion.nombre")
    @Mapping(target = "paisIso2", source = "visitante.pais.iso2")
    UsuarioGetDTO usuarioToUsuarioGetDTO(Usuario usuario, Visitante visitante, List<AccesoDTO> accesos);

    @Mapping(target = "tipoIdentificacion", ignore = true)
    void updateUsuarioFromUsuarioUpdateReq(@MappingTarget Usuario usuario, UsuarioUpdateReq usuarioUpdateReq);

    /**
     * Mapea un usuario a un DTO de usuario card.
     * @param usuario
     * @return UsuarioCardDTO con todos los campos mapeados
     */
    UsuarioCardDTO usuarioToUsuarioCardDTO(Usuario usuario);
}
