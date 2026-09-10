package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.notificacion.NotificacionDTO;
import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificacionMapper {

    @Mapping(target = "tipo", source = "tipoNotificacion.nombre")
    @Mapping(target = "establecimientoId", source = "establecimiento.id")
    @Mapping(target = "leida", expression = "java(notificacion.getFechaHoraLectura() != null)")
    NotificacionDTO notificacionToNotificacionDTO(Notificacion notificacion);

    List<NotificacionDTO> notificacionListToNotificacionDTOList(List<Notificacion> notificaciones);
}
