package com.mza_agrotours.backend.mappers.reserva;

import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDetalleDTO;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservaMapper {
    @Mapping(target = "idReserva", source = "id")
    @Mapping(target = "estadoReserva", source = "estadoActual.estadoReserva.nombre.estado")
    @Mapping(target = "cantPersonas", expression = "java(reserva.getReservaDetalles().size())")
    @Mapping(target = "detalleDTOs", source = "reservaDetalles")
    @Mapping(target = "fechaHoraInicio", source = "actividadDia.fechaHoraInicio")
    @Mapping(target = "fechaHoraFin", source = "actividadDia.fechaHoraFin")
    @Mapping(target = "nombreActividad", source = "actividad.nombre")
    @Mapping(target = "ubicacionEstablecimiento", source = "actividad.establecimiento.ubicacion")
    @Mapping(target = "nombreEstablecimiento", source = "actividad.establecimiento.razonSocial")
    @Mapping(target = "fotos", source = "actividad.fotos")
    @Mapping(target = "idActividad", source = "actividad.id")
    @Mapping(target = "idEstablecimiento", source = "actividad.establecimiento.id")
    ConsultarReservaDTO reservaToConsultarReservaDTO(Reserva reserva);

    @Mapping(target = "tipoRangoEtario", ignore = true) // TODO esto sería una variable de ActividadRE -> RE
    ConsultarReservaDetalleDTO reservaDetalleToDTO(ReservaDetalle reservaDetalle);
}
