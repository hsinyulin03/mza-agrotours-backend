package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.reservas.*;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.EstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.TipoIdentificacionInvalidoException;
import com.mza_agrotours.backend.exceptions.UsuarioDeactivatedException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.exceptions.actividad.ActividadDiaNotFound;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotFoundException;
import com.mza_agrotours.backend.exceptions.reservas.ActividadFullException;
import com.mza_agrotours.backend.exceptions.reservas.EstadoReservaNotFoundException;
import com.mza_agrotours.backend.exceptions.reservas.FechaNacimientoInvalidaException;
import com.mza_agrotours.backend.exceptions.reservas.ReservaNotFoundException;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.repositories.actividad.ActividadRespository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.mza_agrotours.backend.entities.reservas.EstadoReservaNombre.PENDIENTE;

@Service
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final EstablecimientoRepository establecimientoRepository;
    private final ActividadRespository actividadRepository;
    private final ParametrosService parametrosService;
    private final VisitanteRepository visitanteRepository;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;

    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper, EstablecimientoRepository establecimientoRepository, ActividadRespository actividadRepository, ParametrosService parametrosService, VisitanteRepository visitanteRepository, TipoIdentificacionRepository tipoIdentificacionRepository) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.establecimientoRepository = establecimientoRepository;
        this.actividadRepository = actividadRepository;
        this.parametrosService = parametrosService;
        this.visitanteRepository = visitanteRepository;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
    }

    /**
     * Devuelve todos los datos necesarios para que un visitante consulte una reserva específica<p></p>
     *
     * @param id UUID de la Reserva
     * @param firebaseUID UID en firebase
     * @return <code>ConsultarReservaDTO</code> con los datos de una reserva
     * @throws ReservaNotFoundException si la reserva no existe o no pertenece al usuario autenticado
     * @throws UsuarioDeactivatedException si el usuario dueño de la reserva está dado de baja
     */
    @Transactional
    public ConsultarReservaDTO getConsultarReserva(UUID id, String firebaseUID){
        // Obtenemos la reserva, si no existe error.
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(ReservaNotFoundException::new);

        Establecimiento establecimiento = establecimientoRepository.findEstablecimientoByActividadId(reserva.getActividad().getId())
                .orElseThrow(EstablecimientoNotFoundException::new);

        // Verificar que la reserva sea del usuario. Si no lo es, NOT FOUND para evitar dar información a no autorizados
         if (!reserva.getVisitante().getUsuario().getFirebaseUID().equals(firebaseUID))
             throw new ReservaNotFoundException();

         // Verificar que el usuario esté de alta
        if (reserva.getVisitante().getUsuario().getFechaHoraBaja() != null)
            throw new UsuarioDeactivatedException();

        // Armamos el DTO
        return reservaMapper.reservaToConsultarReservaDTO(reserva, establecimiento);
    }

    public ConsultarReservaDTO handleIniciarReserva(RealizarReservaDTO realizarReservaDTO, String firebaseUID){
        LocalDateTime fechaHoraActual = LocalDateTime.now();

        // Gettear al usuario y verificarlo - Fecha hora baja
        Visitante visitante = visitanteRepository.findBuyUsuarioFirebaseUID(firebaseUID)
                .orElseThrow(() -> new UsuarioNotFound("El usuario no pudo ser encontrado"));
        if (visitante.getUsuario().getFechaHoraBaja() != null)
            throw new UsuarioDeactivatedException();

        // Gettear la actividad, chequear que esté activa
        Actividad actividad = actividadRepository.getActividadByDiaActividadId(UUID.fromString(realizarReservaDTO.diaActividadId()))
                .orElseThrow(ActividadNotFoundException::new);
        if (actividad.getFechaHoraBaja() != null || actividad.getEstado().getNombre() != EstadoActividadNombre.PUBLICADO)
            throw new ActividadNotActiveException();

        // Gettear los ActividadRangoEtario activos
        List<ActividadRangoEtario> ares = actividad.getActividadRangoEtarios().stream()
                .filter(are ->
                        are.getFechaValidaDesde().isBefore(fechaHoraActual.toLocalDate())
                            && (are.getFechaValidaHasta() == null
                                || are.getFechaValidaHasta().isBefore(fechaHoraActual.toLocalDate()))
                ).toList();

        // Verificar que se pueda reservar para ese día
        ActividadDia actividadDia = actividad.getActividadesDias().stream()
                .filter(ad -> ad.getId().toString().equals(realizarReservaDTO.diaActividadId()))
                .findFirst().
                orElseThrow(ActividadDiaNotFound::new);

        Integer cantidadReservas = reservaRepository.getCantidadReservasActivasActividadDia(actividadDia.getId());
        if (cantidadReservas + realizarReservaDTO.reservaDetalleList().size() >= actividadDia.getCuposMax())
                throw new ActividadFullException();

        // Crear las nuevas ReservaDetalles y asignarle el estado
        List<ReservaDetalle> reservaDetalles = new ArrayList<>();
        List<RealizarReservaDetalleDTO> dtoDetalles = realizarReservaDTO.reservaDetalleList();
        Integer i = 0;
        BigDecimal totalReserva = BigDecimal.valueOf(0);
        for (RealizarReservaDetalleDTO dtoDetalle: dtoDetalles){
            i++;

            TipoIdentificacion tipoIdentificacion = tipoIdentificacionRepository.findByNombre(TipoIdentificacionNombre.valueOf(dtoDetalle.tipoId()))
                    .orElseThrow(() -> new TipoIdentificacionInvalidoException("El tipo de identificación provisto no es válido"));

            ActividadRangoEtario actividadRangoEtario = null;
            for (ActividadRangoEtario are : ares){
                if (fechaHoraActual.toLocalDate().isBefore(dtoDetalle.fechaNacimiento().plusYears(are.getRangoEtario().getEdadMaxima())) &&
                        fechaHoraActual.toLocalDate().isAfter(dtoDetalle.fechaNacimiento().plusYears(are.getRangoEtario().getEdadMinima()))
                ) actividadRangoEtario = are;
            }
            if (actividadRangoEtario == null) throw new FechaNacimientoInvalidaException();
            totalReserva = totalReserva.add(actividadRangoEtario.getPrecio());
            reservaDetalles.add(reservaMapper.DTOtoReservaDetalle(dtoDetalle, tipoIdentificacion, i, actividadRangoEtario));
        }

        // Crear la nueva reserva
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setReservaDetalles(reservaDetalles);
        nuevaReserva.setFechaHoraInicio(fechaHoraActual);
        nuevaReserva.setFechaHoraExpiracion(fechaHoraActual.plusMinutes(parametrosService.getInstance().getTtlReserva()));

        EstadoReserva estadoReserva = reservaRepository.findEstadoReservaByEstadoReservaNombre(PENDIENTE)
                .orElseThrow(EstadoReservaNotFoundException::new);

        nuevaReserva.cambiarEstado(estadoReserva,fechaHoraActual);

        nuevaReserva.setActividad(actividad);
        nuevaReserva.setActividadDia(actividadDia);

        nuevaReserva.setVisitante(visitante);

        nuevaReserva.setTotalReserva(totalReserva);

        // TODO estos subtotales habría que actualizarlos cuando hablemos con MP para no tener error.
        BigDecimal comisionPropia = totalReserva;
        nuevaReserva.setSubTotalComisionPropia(comisionPropia);
        BigDecimal comisionTransaccion = totalReserva;
        nuevaReserva.setSubtotalComisionTransaccion(comisionTransaccion);
        BigDecimal subtotalProductor = totalReserva.subtract(comisionPropia).subtract(comisionTransaccion);
        nuevaReserva.setSubtotalProductor(subtotalProductor);

        reservaRepository.save(nuevaReserva);

        // TODO probablemente en otro método, no este
        // Comunicarse con mercado pago
        // Si el pago es exitoso, pasa a pagada
        // Si el pago es fallido, pasa a expirada

        Establecimiento establecimiento = establecimientoRepository.findEstablecimientoByActividadId(nuevaReserva.getActividad().getId())
                .orElseThrow(EstablecimientoNotFoundException::new);

        // Avisar al frontend de qué pasó
        return reservaMapper.reservaToConsultarReservaDTO(nuevaReserva, establecimiento);
    }
}
