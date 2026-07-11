package com.mza_agrotours.backend.mappers;

import com.google.firebase.auth.UserRecord.CreateRequest;
import com.mza_agrotours.backend.dtos.UsuarioCreateReq;
import com.mza_agrotours.backend.dtos.UsuarioGetDTO;
import com.mza_agrotours.backend.entities.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    @Mapping(target = "tipoIdentificacion", ignore = true)
    Usuario usuarioCreateReqToUsuario(UsuarioCreateReq usuarioCreateReq);

    @Mapping(target = "tipoIdentificacion", source = "tipoIdentificacion.nombre")
    UsuarioGetDTO usuarioToUsuarioGetDTO(Usuario usuario);

    default CreateRequest usuarioCreateReqToFirebaseCreateRequest(UsuarioCreateReq usuarioCreateReq) {
        CreateRequest request = new CreateRequest()
                .setEmail(usuarioCreateReq.getEmail())
                .setPassword(usuarioCreateReq.getPassword())
                .setDisplayName(usuarioCreateReq.getNombre());

        String telefono = usuarioCreateReq.getTelefono();
        if (telefono != null && !telefono.isBlank()) {
            request.setPhoneNumber(telefono);
        }
        return request;
    }
}
