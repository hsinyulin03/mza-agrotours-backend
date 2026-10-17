package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.pago.DetalleReembolsoAdminDTO;
import com.mza_agrotours.backend.dtos.pago.ListarReembolsoAdminDTO;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.pago.ReembolsoSistemaPago;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

// Usa ReservaMapper para el pago del sistema, los contactos y las personas de la reserva (US RESE-10)
@Mapper(componentModel = "spring", uses = ReservaMapper.class)
public interface ReembolsoMapper {
    // US RESE-09
    @Mapping(target = "idReembolso", source = "reembolso.id")
    @Mapping(target = "estado", source = "reembolso.estadoActual.estadoReembolso.nombre")
    @Mapping(target = "fechaHoraEstado", source = "reembolso.estadoActual.fechaHoraInicio")
    @Mapping(target = "nombreEstablecimiento", source = "reembolso.reserva.actividad.establecimiento.nombre")
    @Mapping(target = "nombreProductorLider", source = "reembolso.reserva.actividad.establecimiento.titular.usuario.nombre")
    @Mapping(target = "idDeuda", source = "idDeuda")
    ListarReembolsoAdminDTO reembolsoToListarReembolsoAdminDTO(Reembolso reembolso, UUID idDeuda);

    @Mapping(target = "idReembolso", source = "reembolso.id")
    @Mapping(target = "fechaHoraPedido", source = "reembolso.fechaHoraPedido")
    @Mapping(target = "montoReembolso", source = "reembolso.montoReembolso")
    @Mapping(target = "estado", source = "reembolso.estadoActual.estadoReembolso.nombre")
    @Mapping(target = "fechaHoraEstado", source = "reembolso.estadoActual.fechaHoraInicio")
    @Mapping(target = "nombreEstablecimiento", source = "reembolso.reserva.actividad.establecimiento.nombre")
    @Mapping(target = "idReserva", source = "reembolso.reserva.id")
    @Mapping(target = "nombreActividad", source = "reembolso.reserva.actividad.nombre")
    @Mapping(target = "actividadFechaHoraInicio", source = "reembolso.reserva.actividadDia.fechaHoraInicio")
    @Mapping(target = "actividadFechaHoraFin", source = "reembolso.reserva.actividadDia.fechaHoraFin")
    @Mapping(target = "visitante", source = "reembolso.reserva.visitante.usuario")
    @Mapping(target = "productorLider", source = "reembolso.reserva.actividad.establecimiento.titular.usuario")
    @Mapping(target = "personas", source = "reembolso.reserva.reservaDetalles")
    @Mapping(target = "pagoReembolsoSistema", source = "reembolsoSistemaPago")
    @Mapping(target = "idDeuda", source = "idDeuda")
    DetalleReembolsoAdminDTO reembolsoToDetalleReembolsoAdminDTO(Reembolso reembolso, ReembolsoSistemaPago reembolsoSistemaPago, UUID idDeuda);
}
