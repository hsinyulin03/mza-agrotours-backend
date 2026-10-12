package com.mza_agrotours.backend.mappers.reserva;

import com.mza_agrotours.backend.dtos.pago.ContactoReembolsoDTO;
import com.mza_agrotours.backend.dtos.pago.DetalleReembolsoAdminDTO;
import com.mza_agrotours.backend.dtos.pago.ListarReembolsoAdminDTO;
import com.mza_agrotours.backend.dtos.pago.PagoReembolsoSistemaDTO;
import com.mza_agrotours.backend.dtos.pago.PersonaReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.ConsultarReservaDetalleDTO;
import com.mza_agrotours.backend.dtos.reservas.ListarReservaDTO;
import com.mza_agrotours.backend.dtos.reservas.RealizarReservaDetalleDTO;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.pago.ReembolsoSistemaPago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservaMapper {
    // US RESE-01
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nombre", source = "dto.nombreApellido")
    @Mapping(target = "identificacion", source = "dto.identificacion")
    @Mapping(target = "tipoIdentificacion", source = "tipoIdentificacion")
    @Mapping(target = "fechaNacimiento", source = "dto.fechaNacimiento")
    @Mapping(target = "renglon", source = "renglon")
    @Mapping(target = "actividadRangoEtario", source = "are")
    @Mapping(target = "subtotal", source = "are.precio")
    ReservaDetalle DTOtoReservaDetalle(RealizarReservaDetalleDTO dto, TipoIdentificacion tipoIdentificacion, Integer renglon, ActividadRangoEtario are);

    // US RESE-04
    @Mapping(target = "idReserva", source = "reserva.id")
    @Mapping(target = "estadoReserva", source = "reserva.estadoActual.estadoReserva.nombre.estado")
    @Mapping(target = "cantPersonas", expression = "java(reserva.getReservaDetalles().size())")
    @Mapping(target = "detalleDTOs", source = "reserva.reservaDetalles")
    @Mapping(target = "actividadFechaHoraInicio", source = "reserva.actividadDia.fechaHoraInicio")
    @Mapping(target = "actividadFechaHoraFin", source = "reserva.actividadDia.fechaHoraFin")
    @Mapping(target = "nombreActividad", source = "reserva.actividad.nombre")
    @Mapping(target = "ubicacionEstablecimiento", source = "reserva.actividad.establecimiento.ubicacion")
    @Mapping(target = "nombreEstablecimiento", source = "reserva.actividad.establecimiento.razonSocial")
    @Mapping(target = "idActividad", source = "reserva.actividad.id")
    @Mapping(target = "idEstablecimiento", source = "reserva.actividad.establecimiento.id")
    @Mapping(target = "calificada", expression = "java(reserva.getCalificacion() != null)")
    ConsultarReservaDTO reservaToConsultarReservaDTO(Reserva reserva);

    @Mapping(target = "tipoRangoEtario", source = "reservaDetalle.actividadRangoEtario.nombre")
    ConsultarReservaDetalleDTO reservaDetalleToDTO(ReservaDetalle reservaDetalle);

    @Mapping(target = "idReserva", source = "reserva.id")
    @Mapping(target = "fechaHoraInicioReserva", source = "reserva.fechaHoraInicio")
    @Mapping(target = "estadoReserva", source = "reserva.estadoActual.estadoReserva.nombre.estado")
    @Mapping(target = "cantPersonas", expression = "java(reserva.getReservaDetalles().size())")
    @Mapping(target = "actividadFechaHoraInicio", source = "reserva.actividadDia.fechaHoraInicio")
    @Mapping(target = "actividadFechaHoraFin", source = "reserva.actividadDia.fechaHoraFin")
    @Mapping(target = "nombreActividad", source = "reserva.actividad.nombre")
    @Mapping(target = "ubicacionEstablecimiento", source = "reserva.actividad.establecimiento.ubicacion")
    @Mapping(target = "nombreEstablecimiento", source = "reserva.actividad.establecimiento.razonSocial")
    @Mapping(target = "idActividad", source = "reserva.actividad.id")
    ListarReservaDTO reservaToListarReservaDTO(Reserva reserva);

    // US RESE-09
    @Mapping(target = "idReembolso", source = "reembolso.id")
    @Mapping(target = "estado", source = "reembolso.estadoActual.estadoReembolso.nombre")
    @Mapping(target = "fechaHoraEstado", source = "reembolso.estadoActual.fechaHoraInicio")
    @Mapping(target = "nombreEstablecimiento", source = "reembolso.reserva.actividad.establecimiento.nombre")
    @Mapping(target = "nombreProductorLider", source = "reembolso.reserva.actividad.establecimiento.titular.usuario.nombre")
    ListarReembolsoAdminDTO reembolsoToListarReembolsoAdminDTO(Reembolso reembolso);

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
    DetalleReembolsoAdminDTO reembolsoToDetalleReembolsoAdminDTO(Reembolso reembolso, ReembolsoSistemaPago reembolsoSistemaPago);

    // US RESE-10
    @Mapping(target = "idPago", source = "pago.id")
    @Mapping(target = "fechaHoraPago", source = "pago.fechaHoraPago")
    @Mapping(target = "idPagoExterno", source = "pago.idTransaccionExterna")
    @Mapping(target = "metodoPago", source = "metodoPagoReal")
    @Mapping(target = "administrador", source = "cargadoPorAdmin.usuario.nombre")
    PagoReembolsoSistemaDTO reembolsoSistemaPagoToPagoReembolsoSistemaDTO(ReembolsoSistemaPago reembolsoSistemaPago);

    ContactoReembolsoDTO usuarioToContactoReembolsoDTO(Usuario usuario);

    PersonaReservaDTO reservaDetalleToPersonaReservaDTO(ReservaDetalle reservaDetalle);

    // Las personas van en el orden en que se cargaron en la reserva
    default List<PersonaReservaDTO> reservaDetallesToPersonasReservaDTO(List<ReservaDetalle> reservaDetalles) {
        return reservaDetalles.stream()
                .sorted(Comparator.comparing(ReservaDetalle::getRenglon))
                .map(this::reservaDetalleToPersonaReservaDTO)
                .toList();
    }
}
