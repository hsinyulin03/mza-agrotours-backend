package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.notificacion.TokenNotificacionResponseDTO;
import com.mza_agrotours.backend.entities.notificacion.TokenNotificacion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TokenNotificacionMapper {
    TokenNotificacionResponseDTO tokenNotificacionToTokenNotificacionResponseDTO(TokenNotificacion tokenNotificacion);
}
