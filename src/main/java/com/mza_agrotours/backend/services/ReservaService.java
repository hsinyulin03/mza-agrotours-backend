package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.reservas.*;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.exceptions.EstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.TipoIdentificacionInvalidoException;
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
import com.mza_agrotours.backend.services.pago.EstrategiaPago;
import com.mza_agrotours.backend.services.pago.EstrategiaPagoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.*;

import static com.mza_agrotours.backend.enums.EstadoReservaNombre.EXPIRADA;
import static com.mza_agrotours.backend.enums.EstadoReservaNombre.PAGADA;
import static com.mza_agrotours.backend.enums.EstadoReservaNombre.PENDIENTE;

@Service
public class ReservaService {
    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final EstablecimientoRepository establecimientoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VisitanteRepository visitanteRepository;
    private final ActividadRespository actividadRepository;
    private final ParametrosService parametrosService;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final EstrategiaPagoFactory estrategiaPagoFactory;
    private final ReservaService self;

    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper, EstablecimientoRepository establecimientoRepository, ActividadRespository actividadRepository, ParametrosService parametrosService, UsuarioRepository usuarioRepository, VisitanteRepository visitanteRepository, TipoIdentificacionRepository tipoIdentificacionRepository, EstrategiaPagoFactory estrategiaPagoFactory, @Lazy ReservaService self) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.establecimientoRepository = establecimientoRepository;
        this.usuarioRepository = usuarioRepository;
        this.visitanteRepository = visitanteRepository;
        this.actividadRepository = actividadRepository;
        this.parametrosService = parametrosService;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.estrategiaPagoFactory = estrategiaPagoFactory;
        this.self = self;
    }

    @Transactional
    public ConsultarReservaDTO getConsultarReserva(UUID id, String emailUsuario){

        // Gettear al usuario y visitante
        Usuario usuario = usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        Visitante visitante = visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);

        // Obtenemos la reserva, si no existe error.
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(ReservaNotFoundException::new);

        // Verificar que la reserva sea del usuario. Si no lo es, NOT FOUND para evitar dar información a no autorizados
         if (!reserva.getVisitante().getId().equals(visitante.getId()))
             throw new ReservaNotFoundException();

        // Buscar el establecimiento de esa actividad
        Establecimiento establecimiento = establecimientoRepository.findEstablecimientoByActividadId(reserva.getActividad().getId())
                .orElseThrow(EstablecimientoNotFoundException::new);

        // Armamos el DTO
        return reservaMapper.reservaToConsultarReservaDTO(reserva, establecimiento);
    }

    @Transactional(readOnly = true)
    public List<ListarReservaDTO> getListarReservas(String emailUsuario){

        List<ListarReservaDTO> dtos = new ArrayList<>();

        // Gettear al usuario y visitante
        Usuario usuario = usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        Visitante visitante = visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);

        // Obtenemos las reservas. Si está vacío devolvemos el array vacío
        List<Reserva> reservas = reservaRepository.findByVisitanteId(visitante.getId());
        if (reservas.isEmpty())
            return dtos;

        // Obtenemos las id de las actividades que ha reservado
        List<UUID> uuidsActividad = new ArrayList<>();
        reservas.forEach(reserva -> uuidsActividad.add(reserva.getActividad().getId()));

        // Buscamos los establecimientos de las actividades en masse
        List<EstablecimientoPorActividad> establecimientosPorActividad = establecimientoRepository.findEstablecimientosByActividadIds(uuidsActividad);

        Map<UUID, Establecimiento> actividadEstablecimientoMap = new HashMap<>();
        establecimientosPorActividad.forEach(establecimientoPorActividad ->
                actividadEstablecimientoMap.put(establecimientoPorActividad.actividadID(), establecimientoPorActividad.establecimiento())
        );

        // Armamos el dto para cada reserva
        for (Reserva reserva: reservas){
            Establecimiento establecimiento =  Optional.ofNullable(actividadEstablecimientoMap.get(reserva.getActividad().getId()))
                    .orElseThrow(EstablecimientoNotFoundException::new);

            dtos.add(reservaMapper.reservaToListarReservaDTO(reserva, establecimiento));
        }

        // Armamos el DTO
        return dtos;
    }

    @Transactional
    public ConsultarReservaDTO handleIniciarReserva(RealizarReservaDTO realizarReservaDTO, String emailUsuario){
        LocalDateTime fechaHoraActual = LocalDateTime.now();

        // Gettear al usuario y visitante
        Usuario usuario = usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));

        Visitante visitante = visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);

        // Gettear la actividad, chequear que esté activa
        Actividad actividad = actividadRepository.getActividadByDiaActividadId(UUID.fromString(realizarReservaDTO.diaActividadId()))
                .orElseThrow(ActividadNotFoundException::new);
        if (actividad.getFechaHoraBaja() != null || actividad.getEstado().getNombre() != EstadoActividadNombre.PUBLICADO)
            throw new ActividadNotActiveException();

        // Gettear los ActividadRangoEtario activos
        List<ActividadRangoEtario> ares = actividad.getActividadRangoEtarios().stream()
                .filter(are ->
                        are.getFechaHoraBaja() == null || are.getFechaHoraBaja().isAfter(fechaHoraActual)
                ).toList();

        // Verificar que se pueda reservar para ese día
        ActividadDia actividadDia = actividad.getActividadesDias().stream()
                .filter(ad -> ad.getId().toString().equals(realizarReservaDTO.diaActividadId()))
                .filter(ad ->
                        ad.getEstadoActual().getEstado().getNombre() == EstadoActividadDiaNombre.ACTIVA ||
                        ad.getEstadoActual().getEstado().getNombre() == EstadoActividadDiaNombre.REPROGRAMADA
                )
                .filter(ad -> ad.getFechaHoraInicio().isAfter(fechaHoraActual))    // NOTE una actividad reprogramada se le cambia la fechaHoraInicio, no?
                .findFirst().
                orElseThrow(ActividadDiaNotFound::new);

        int cantidadReservas = reservaRepository.getCuposReservadosActividadDia(actividadDia.getId()).intValue();
        if (cantidadReservas + realizarReservaDTO.reservaDetalleList().size() > actividadDia.getCuposMax())
                throw new ActividadFullException();

        // Crear las nuevas ReservaDetalles y asignarle el estado
        List<ReservaDetalle> reservaDetalles = new ArrayList<>();
        List<RealizarReservaDetalleDTO> dtoDetalles = realizarReservaDTO.reservaDetalleList();
        Integer renglonReserva = 0;
        BigDecimal totalReserva = BigDecimal.valueOf(0);
        for (RealizarReservaDetalleDTO dtoDetalle: dtoDetalles){
            renglonReserva++;

            TipoIdentificacion tipoIdentificacion = tipoIdentificacionRepository.findByNombre(TipoIdentificacionNombre.valueOf(dtoDetalle.tipoIdentificacion()))
                    .orElseThrow(() -> new TipoIdentificacionInvalidoException("El tipo de identificación provisto no es válido"));

            ActividadRangoEtario actividadRangoEtario = null;
            for (ActividadRangoEtario are : ares){
                if (fechaHoraActual.toLocalDate().isBefore(dtoDetalle.fechaNacimiento().plusYears(are.getEdadMaxima())) &&
                        fechaHoraActual.toLocalDate().isAfter(dtoDetalle.fechaNacimiento().plusYears(are.getEdadMinima()))
                ) actividadRangoEtario = are;
            }
            if (actividadRangoEtario == null) throw new FechaNacimientoInvalidaException();
            totalReserva = totalReserva.add(actividadRangoEtario.getPrecio());
            reservaDetalles.add(reservaMapper.DTOtoReservaDetalle(dtoDetalle, tipoIdentificacion, renglonReserva, actividadRangoEtario));
        }

        // Crear la nueva reserva
        Reserva nuevaReserva = new Reserva();
        nuevaReserva.setReservaDetalles(reservaDetalles);
        nuevaReserva.setFechaHoraInicio(fechaHoraActual);
        nuevaReserva.setFechaHoraExpiracion(fechaHoraActual.plusMinutes(parametrosService.getInstance().getTtlReserva()));

        EstadoReserva estadoReserva = reservaRepository.findEstadoReservaByEstadoReservaNombre(PENDIENTE)
                .orElseThrow(() -> new EstadoReservaNotFoundException(EstadoReservaNombre.PENDIENTE));

        nuevaReserva.cambiarEstado(estadoReserva,fechaHoraActual);

        nuevaReserva.setActividad(actividad);
        nuevaReserva.setActividadDia(actividadDia);

        nuevaReserva.setVisitante(visitante);

        nuevaReserva.setTotalReserva(totalReserva);

        MetodoPago metodoPago = MetodoPago.MANUAL;  // TODO Cambiar esto para cuando se use el medio de pago real

        EstrategiaPago estrategiaPago = estrategiaPagoFactory.get(metodoPago);
        Pago pago = estrategiaPago.procesarPago(nuevaReserva);

        // Si el pago ya fue aprobado (manual), la reserva pasa a pagada.
        // Si queda pendiente (Mercado Pago), la reserva sigue pendiente hasta la confirmación por webhook (otro método).
        if (pago.getEstadoActual().getEstadoPago().getNombre() == EstadoPagoNombre.APROBADO) {
            // Cambiar estado
            EstadoReserva estadoPagada = reservaRepository.findEstadoReservaByEstadoReservaNombre(PAGADA)
                    .orElseThrow(() -> new EstadoReservaNotFoundException(EstadoReservaNombre.PAGADA));
            nuevaReserva.cambiarEstado(estadoPagada, fechaHoraActual);

            // Eliminar la fecha de expiración
            nuevaReserva.setFechaHoraExpiracion(null);
        }

        reservaRepository.save(nuevaReserva);

        Establecimiento establecimiento = establecimientoRepository.findEstablecimientoByActividadId(nuevaReserva.getActividad().getId())
                .orElseThrow(EstablecimientoNotFoundException::new);

        // Avisar al frontend de qué pasó
        return reservaMapper.reservaToConsultarReservaDTO(nuevaReserva, establecimiento);
    }

    @Transactional(readOnly = true)
    public void expirarReservas(){
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservas = reservaRepository.findReservasExpiradas(ahora);

        EstadoReserva estadoReserva = reservaRepository.findEstadoReservaByEstadoReservaNombre(EXPIRADA)
                .orElseThrow(() -> new EstadoReservaNotFoundException(EstadoReservaNombre.EXPIRADA));

        List<String> idFallidas = new ArrayList<>();

        for (Reserva r : reservas){
            try{
                self.expirarReservaIndividual(r, estadoReserva, ahora);
            } catch (Exception e) {
                idFallidas.add(r.getId().toString());
            }
        }

        log.info("Se expiraron {}/{} reservas. Las reservas no expiradas fueron {}, con ids: {}",
                reservas.size() - idFallidas.size(), reservas.size(), idFallidas.size(), idFallidas);
    }

    /**
     * Expira una única reserva en su propia transacción, para que un fallo al guardar
     * una reserva no revierta las expiraciones ya confirmadas de las demás.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expirarReservaIndividual(Reserva r, EstadoReserva estadoReserva, LocalDateTime ahora){
        r.cambiarEstado(estadoReserva, ahora);
        reservaRepository.save(r);
    }
}
