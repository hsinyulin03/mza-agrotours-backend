package com.mza_agrotours.backend.mappers.reserva;

import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDetalleDTO;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservaMapper {
    @Mapping(target = "idReserva", source = "reserva.id")
    @Mapping(target = "estadoReserva", source = "reserva.estadoActual.estadoReserva.nombre.estado")
    @Mapping(target = "cantPersonas", expression = "java(reserva.getReservaDetalles().size())")
    @Mapping(target = "detalleDTOs", source = "reserva.reservaDetalles")
    @Mapping(target = "fechaHoraInicio", source = "reserva.actividadDia.fechaHoraInicio")
    @Mapping(target = "fechaHoraFin", source = "reserva.actividadDia.fechaHoraFin")
    @Mapping(target = "nombreActividad", source = "reserva.actividad.nombre")
    @Mapping(target = "ubicacionEstablecimiento", source = "establecimiento.ubicacion")
    @Mapping(target = "nombreEstablecimiento", source = "establecimiento.razonSocial")
    @Mapping(target = "idActividad", source = "reserva.actividad.id")
    @Mapping(target = "idEstablecimiento", source = "establecimiento.id")
    ConsultarReservaDTO reservaToConsultarReservaDTO(Reserva reserva, Establecimiento establecimiento);

    @Mapping(target = "tipoRangoEtario", source = "reservaDetalle.actividadRangoEtario.nombre")
    ConsultarReservaDetalleDTO reservaDetalleToDTO(ReservaDetalle reservaDetalle);
}
