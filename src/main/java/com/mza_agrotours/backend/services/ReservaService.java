package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.config.RutasNotificacionesFront;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
import com.mza_agrotours.backend.dtos.reservas.*;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoReembolso;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.enums.*;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.enums.EstadoActividadDiaNombre;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.exceptions.TipoIdentificacionInvalidoException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.exceptions.actividad.ActividadDiaNotFound;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotFoundException;
import com.mza_agrotours.backend.exceptions.pago.EstadoPagoNotFoundException;
import com.mza_agrotours.backend.exceptions.pago.EstadoReembolsoNotFoundException;
import com.mza_agrotours.backend.exceptions.pago.PasarelaPagoException;
import com.mza_agrotours.backend.exceptions.pago.ReembolsoMakingException;
import com.mza_agrotours.backend.exceptions.pago.ReembolsoStateException;
import com.mza_agrotours.backend.exceptions.reservas.ActividadFullException;
import com.mza_agrotours.backend.exceptions.reservas.EstadoReservaNotFoundException;
import com.mza_agrotours.backend.exceptions.reservas.FechaNacimientoInvalidaException;
import com.mza_agrotours.backend.exceptions.reservas.ReservaNotFoundException;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.*;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.*;

import static com.mza_agrotours.backend.enums.EstadoReservaNombre.*;

@Service
public class ReservaService {
    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private final ReservaService self;
    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final UsuarioRepository usuarioRepository;
    private final VisitanteRepository visitanteRepository;
    private final ActividadRepository actividadRepository;
    private final ParametrosService parametrosService;
    private final TipoIdentificacionRepository tipoIdentificacionRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final EstrategiaPagoFactory estrategiaPagoFactory;
    private final NotificacionService notificacionService;
    private final EstadoReembolsoRepository estadoReembolsoRepository;
    private final ReembolsoRepository reembolsoRepository;

    public ReservaService(ReservaRepository reservaRepository, ReservaMapper reservaMapper, ActividadRepository actividadRepository, ParametrosService parametrosService, UsuarioRepository usuarioRepository, VisitanteRepository visitanteRepository, TipoIdentificacionRepository tipoIdentificacionRepository, EstrategiaPagoFactory estrategiaPagoFactory, @Lazy ReservaService self, EstadoPagoRepository estadoPagoRepository, NotificacionService notificacionService, EstadoReembolsoRepository estadoReembolsoRepository, ReembolsoRepository reembolsoRepository) {
        this.self = self;
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.usuarioRepository = usuarioRepository;
        this.visitanteRepository = visitanteRepository;
        this.actividadRepository = actividadRepository;
        this.parametrosService = parametrosService;
        this.tipoIdentificacionRepository = tipoIdentificacionRepository;
        this.estadoPagoRepository = estadoPagoRepository;
        this.estrategiaPagoFactory = estrategiaPagoFactory;
        this.notificacionService = notificacionService;
        this.estadoReembolsoRepository = estadoReembolsoRepository;
        this.reembolsoRepository = reembolsoRepository;
    }

    @Transactional
    public ConsultarReservaDTO getConsultarReserva(String idReserva, String emailUsuario){

        // Gettear al usuario y visitante
        Usuario usuario = getUsuario(emailUsuario);

        Visitante visitante = getVisitante(usuario);

        // Obtenemos la reserva, si no existe o no es del visitante, error.
        Reserva reserva = getReserva(idReserva, visitante);

        // Armamos el DTO
        return reservaMapper.reservaToConsultarReservaDTO(reserva);
    }

    @Transactional(readOnly = true)
    public List<ListarReservaDTO> getListarReservas(String emailUsuario){

        List<ListarReservaDTO> dtos = new ArrayList<>();

        // Gettear al usuario y visitante
        Usuario usuario = getUsuario(emailUsuario);

        Visitante visitante = getVisitante(usuario);

        // Obtenemos las reservas. Si está vacío devolvemos el array vacío
        List<Reserva> reservas = reservaRepository.findByVisitanteId(visitante.getId());
        if (reservas.isEmpty())
            return dtos;

        // Armamos el dto para cada reserva
        for (Reserva reserva: reservas){
            dtos.add(reservaMapper.reservaToListarReservaDTO(reserva));
        }

        // Armamos el DTO
        return dtos;
    }

    /**
     * Inicia una nueva reserva para el usuario indicado: valida la actividad y el día elegido, calcula el
     * rango etario y el precio de cada detalle, verifica cupo disponible y crea la reserva en estado
     * "Pendiente". Dispara el procesamiento de pago mediante la estrategia correspondiente al método de pago;
     * si el pago queda aprobado de forma inmediata (pago manual), la reserva pasa directamente a "Pagada",
     * caso contrario queda pendiente de confirmación.
     * <p>Si el visitante ya tenía una reserva pendiente para el mismo día de actividad, esta se
     * libera (se expira) antes de crear la nueva.
     *
     * @param realizarReservaDTO datos de la reserva a crear: día de actividad y detalle de cada visitante
     * @param emailUsuario email del usuario autenticado que realiza la reserva
     * @return DTO con la reserva creada y el ID de preferencia de pago generado
     * @throws UsuarioNotFound si no existe un usuario activo con ese email
     * @throws ActividadNotFoundException si no existe una actividad para el día indicado
     * @throws ActividadNotActiveException si la actividad o el establecimiento no están disponibles para reservar
     * @throws ActividadDiaNotFound si el día de actividad no existe o no está disponible para reservar
     * @throws ActividadFullException si no hay cupo suficiente para la cantidad de detalles solicitados
     * @throws TipoIdentificacionInvalidoException si algún visitante tiene un tipo de identificación inválido
     * @throws FechaNacimientoInvalidaException si la fecha de nacimiento de algún detalle no corresponde a ningún rango etario activo
     */
    @Transactional
    public IniciarReservaDTO handleIniciarReserva(RealizarReservaDTO realizarReservaDTO, String emailUsuario){
        LocalDateTime fechaHoraActual = LocalDateTime.now();

        // Gettear al usuario y visitante
        Usuario usuario = getUsuario(emailUsuario);

        Visitante visitante = getVisitante(usuario);

        // Verificar si la reserva es duplicada (ya hay "Pendiente" de este Visitante para este ActividadDia). En tal caso expirar la vieja y seguir con la nueva
        Optional<Reserva> reservaDuplicada = reservaRepository.findByVisitanteIdAndActividadDiaId(visitante.getId(), UUID.fromString(realizarReservaDTO.diaActividadId()));

        reservaDuplicada.ifPresent(this::liberarCupoReserva);

        // Gettear la actividad, chequear que esté activa
        Actividad actividad = actividadRepository.getActividadByDiaActividadId(UUID.fromString(realizarReservaDTO.diaActividadId()))
                .orElseThrow(ActividadNotFoundException::new);
        if (actividad.getFechaHoraBaja() != null
                || actividad.getEstado().getNombre() != EstadoActividadNombre.PUBLICADO
                || actividad.getEstablecimiento().getEstadoActual()
                    .getEstadoEstablecimiento()
                    .getNombre().equals(EstadoEstablecimientoNombre.SUSPENDIDO))
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

        EstadoReserva estadoReserva = getEstadoReserva(PENDIENTE);

        nuevaReserva.cambiarEstado(estadoReserva,fechaHoraActual);

        nuevaReserva.setActividad(actividad);
        nuevaReserva.setActividadDia(actividadDia);

        nuevaReserva.setVisitante(visitante);

        nuevaReserva.setTotalReserva(totalReserva);

        MetodoPago metodoPago = MetodoPago.MERCADO_PAGO;

        reservaRepository.saveAndFlush(nuevaReserva);

        EstrategiaPago estrategiaPago = estrategiaPagoFactory.get(metodoPago);
        PagoStrategyDTO pagoStratDTO = estrategiaPago.procesarPago(nuevaReserva);
        Pago pago = pagoStratDTO.pago();
        String preferenceID = pagoStratDTO.preferenceID();

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

        // Avisar al frontend de qué pasó
        return new IniciarReservaDTO(reservaMapper.reservaToConsultarReservaDTO(nuevaReserva), preferenceID);
    }

    /**
     * Tarea programada que busca todas las reservas "Pendiente" cuya fecha y hora de expiración ya pasó,
     * las marca como "Expirada" y les quita la fecha de expiración para que no sean revisadas nuevamente.
     * Cada reserva se cambia y guarda en su propia transacción, por lo que el fallo de una no afecta a las demás.
     * Las reservas que no pudieron expirarse quedan registradas en el log para su seguimiento.
     */
    @Transactional(readOnly = true)
    public void expirarReservas(){
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservas = reservaRepository.findReservasExpiradas(ahora);

        EstadoReserva estadoReserva = getEstadoReserva(EXPIRADA);

        List<String> idFallidas = new ArrayList<>();

        for (Reserva r : reservas){
            try{
                r.setFechaHoraExpiracion(null);
                self.cambiarEstadoReservaYGuardar(r, estadoReserva, ahora);
            } catch (Exception e) {
                idFallidas.add(r.getId().toString());
            }
        }

        log.info("Se expiraron {}/{} reservas. Las reservas no expiradas fueron {}, con ids: {}",
                reservas.size() - idFallidas.size(), reservas.size(), idFallidas.size(), idFallidas);
    }

    /**
     * Workaround al todavía no tener notificaciones webhook de las pasarelas.
     * <p>
     * Tarea programada que busca todas las reservas "Pendiente" aún no expiradas y consulta, mediante la
     * estrategia del método de pago de cada una, si su pago fue aprobado.
     * <p>
     * Cuando encuentra un pago aprobado, guarda el ID de la transacción externa, marca el pago como "Aprobado",
     * la reserva como "Pagada", le quita la fecha de expiración e invalida la sesión de cobro para reducir la
     * chance de un pago duplicado. Los errores de comunicación con la pasarela o de backend al procesar una
     * reserva particular no interrumpen el procesamiento del resto y quedan registrados en el log.
     */
    @Transactional(readOnly = true)
    public void pagarReservas(){
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservas = reservaRepository.findReservasPendientes(ahora);

        EstadoReserva estadoReserva = getEstadoReserva(PAGADA);
        EstadoPago estadoPago = getEstadoPago(EstadoPagoNombre.APROBADO);

        List<String> idFallidas = new ArrayList<>(); // Array de las id de reserva que fallaron en pasarse a pagada
        int pagosExitosos = 0; // Cantidad de pagos exitosos

        for (Reserva r : reservas){
            Pago pago = r.getPago();

            try{
                ResultadoConsultaPagoDTO resultado = getEstrategiaPago(pago).consultarPago(pago);
                if (!resultado.aprobado()) continue;

                pago.setIdTransaccionExterna(resultado.idTransaccionExterna()); // Necesario para reembolsar
                pago.cambiarEstado(estadoPago, ahora);  //Estado del pago

                r.setFechaHoraExpiracion(null);         // FHExpiración de la reserva
                self.cambiarEstadoReservaYGuardar(r, estadoReserva, ahora); // Estado de la reserva

                // Invalidar la sesión de cobro (para menor chance que se pague 2 veces)
                cancelarCheckout(pago, ahora);

                pagosExitosos++; // Contador de reservas pagadas para el log
            } catch (PasarelaPagoException e) {
                log.warn("Error de la pasarela consultando el pago de la reserva {}", r.getId(), e);
                idFallidas.add(r.getId().toString());
            }
            catch (Exception e) {
                log.warn("Error de backend checkeando reservas pagas {}", r.getId(), e);
                idFallidas.add(r.getId().toString());
            }
        }

        log.info("Se pagaron {}/{} reservas pendientes encontradas. Las reservas con cambio fallido fueron {}, con ids: {}",
                pagosExitosos, reservas.size(), idFallidas.size(), idFallidas);
    }

    // TODO hay que ver el cancelaciones (de pagos) mediante endpoint "Crear cancelación" de mercado pago
    /**
     * Cancela el pago pendiente de una reserva a pedido del usuario: libera el cupo de la reserva
     * (pasándola a "Expirada") y expira la preference correspondiente en Mercado Pago.
     *
     * @param preferenceId ID externo de la preference de Mercado Pago asociada al pago de la reserva
     * @param emailUsuario email del usuario autenticado dueño de la reserva
     * @throws UsuarioNotFound si no existe un usuario activo con ese email
     * @throws ReservaNotFoundException si no existe una reserva con ese preferenceId o si no pertenece al usuario
     */
    @Transactional
    public void handleCancelarPago(String preferenceId, String emailUsuario){

        // Gettear al usuario y visitante
        Usuario usuario = getUsuario(emailUsuario);
        Visitante visitante = getVisitante(usuario);

        Optional<Reserva> optReserva = reservaRepository.findByPagoWithIdCheckoutExterno(preferenceId);
        if (optReserva.isEmpty()) {
            throw new ReservaNotFoundException();
        }
        Reserva reserva = optReserva.get();

        // Confirmar que sea del usuario
        if (reserva.getVisitante() != visitante){
            throw new ReservaNotFoundException();
        }
        liberarCupoReserva(reserva);
        reservaRepository.save(reserva);
    }

    @Transactional
    public void cancelarReservasPorBajaDeActividad(Actividad actividad,LocalDateTime ahora) {
        List<Reserva> pendientes = reservaRepository.findPendientesByActividadId(actividad.getId());
        if (pendientes.isEmpty()) return;

        EstadoReserva cancelada = getEstadoReserva(CANCELADA_SIN_REEMBOLSO);

        for (Reserva r : pendientes) {
            r.setFechaHoraExpiracion(null);
            r.cambiarEstado(cancelada, ahora);
            cancelarCheckout(r.getPago(), ahora);
            notificacionService.crearNotificacion(
                    r.getVisitante().getUsuario(),
                    TipoNotificacionNombre.RESERVA_CANCELADA_POR_BAJA_ACTIVIDAD,
                    r.getActividad().getEstablecimiento(),
                    RutasNotificacionesFront.detalleReserva(r.getId()),
                    actividad.getNombre(), r.getActividadDia().getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        log.info("Se cancelaron {} reservas pendientes por baja de la actividad {}", pendientes.size(), actividad.getId());
    }

    @Transactional
    public IniciarReembolsoDTO handleCancelarReservaCondicion(String reservaId, String emailUsuario){
        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = getUsuario(emailUsuario);
        Visitante visitante = getVisitante(usuario);

        // Obtenemos la reserva, si no existe o no es del visitante, error.
        Reserva reserva = getReserva(reservaId, visitante);

        // Validamos que sea una reserva en Pagada
        EstadoReservaNombre estadoReserva = reserva.getEstadoActual().getEstadoReserva().getNombre();
        if (estadoReserva != PAGADA){
            throw new ReembolsoStateException(estadoReserva.getEstado());
        }

        ActividadDia actividadDia = reserva.getActividadDia();

        Integer diasMinReembolso = parametrosService.getInstance().getDiasMinReembolso();

        IniciarReembolsoDTO respuesta;

        // CASOS DE REEMBOLSO
        if (
            // un ActividadDia que está a más de diasMinReembolso de ocurrir
            ahora.plusDays(diasMinReembolso).isBefore(actividadDia.getFechaHoraInicio())
            // un ActividadDia reprogramado
            || actividadDia.getEstadoActual().getEstado().getNombre() == EstadoActividadDiaNombre.REPROGRAMADA
        ){
            respuesta = new IniciarReembolsoDTO("CancelacionConReembolso");
        }
        // CASOS DE NO REEMBOLSO
        else{
            respuesta = new IniciarReembolsoDTO("CancelacionSinReembolso");
        }
        return respuesta;
    }

    @Transactional
    public IniciarReembolsoDTO handleCancelarReserva(String reservaId, String emailUsuario){
        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = getUsuario(emailUsuario);
        Visitante visitante = getVisitante(usuario);

        // Obtenemos la reserva, si no existe o no es del visitante, error.
        Reserva reserva = getReserva(reservaId, visitante);

        // Validamos que sea una reserva en Pagada
        EstadoReservaNombre estadoReserva = reserva.getEstadoActual().getEstadoReserva().getNombre();
        if (estadoReserva != PAGADA){
            throw new ReembolsoStateException(estadoReserva.getEstado());
        }

        ActividadDia actividadDia = reserva.getActividadDia();
        Pago pago = reserva.getPago();

        Integer diasMinReembolso = parametrosService.getInstance().getDiasMinReembolso();

        IniciarReembolsoDTO respuesta;

        // CASOS DE REEMBOLSO
        if (
                // un ActividadDia que está a más de diasMinReembolso de ocurrir
                ahora.plusDays(diasMinReembolso).isBefore(actividadDia.getFechaHoraInicio())
                // un ActividadDia reprogramado
                || actividadDia.getEstadoActual().getEstado().getNombre() == EstadoActividadDiaNombre.REPROGRAMADA
        ){
            // Traemos los estados que vamos a usar
            EstadoReembolso estadoReembolsoPedido = getEstadoReembolso(EstadoReembolsoNombre.PEDIDO);
            EstadoReserva estadoReservaReembolsoPendiente = getEstadoReserva(CANCELADA_REEMBOLSO_PENDIENTE);

            // Crear el reembolso
            Reembolso reembolso = new Reembolso();
            reembolso.setMontoReembolso(pago.getMontoTotal());
            reembolso.setFechaHoraPedido(ahora);
            reembolso.cambiarEstado(estadoReembolsoPedido, ahora);
            reembolso.setReserva(reserva);

            // Cambiar el estado de la reserva
            reserva.cambiarEstado(estadoReservaReembolsoPendiente, ahora);

            // Hacer el reembolso mediante el método con el que se pagó
            ResultadoReembolsoDTO resultadoReembolso = getEstrategiaPago(pago).reembolsar(pago);
            reembolso.setIdReembolsoExterno(resultadoReembolso.idReembolsoExterno());

            // Resultado en respuesta
            if (resultadoReembolso.aceptado()){
                // Avisar que el proceso marcha bien, avisamos cuando sepamos
                respuesta = new IniciarReembolsoDTO("ExitoReembolso");
            } else {
                // Avisar que hubo un error. Se notificó al propietario
                respuesta = new IniciarReembolsoDTO("ReembolsoPendiente");
            }

            // Guardar
            reservaRepository.save(reserva);
            reembolsoRepository.save(reembolso);

        }
        // CASOS DE NO REEMBOLSO
        else{
            // Cancelar reserva y no hacer reembolso
            EstadoReserva estadoNoReembolsado = getEstadoReserva(CANCELADA_SIN_REEMBOLSO);
            reserva.cambiarEstado(estadoNoReembolsado, ahora);

            respuesta = new IniciarReembolsoDTO("ExitoSinReembolso");
            // Guardar
            reservaRepository.save(reserva);
        }
        // TODO: notificar al productor y al visitante
        return respuesta;
    }

    // AUXILIARES


    /**
     * Libera el cupo ocupado por una reserva: la pasa al estado "Expirada" con fecha de expiración igual
     * al momento actual y expira la preference de Mercado Pago asociada, si existe.
     *
     * @param reserva reserva cuyo cupo se libera
     */
    private void liberarCupoReserva(Reserva reserva){
        LocalDateTime ahora = LocalDateTime.now();

        EstadoReserva estadoReserva = getEstadoReserva(EXPIRADA);

        // Cambiar estado reserva
        reserva.setFechaHoraExpiracion(ahora);
        self.cambiarEstadoReservaYGuardar(reserva, estadoReserva, ahora);
        cancelarCheckout(reserva.getPago(), ahora);

    }

    // TODO: No me gusta mucho que las sesiones de cobro puedan quedar vigentes si hay error, pero por ahora queda así
    /**
     * Invalida la sesión de cobro del pago mediante la estrategia de su método de pago, para que ya no
     * pueda pagarse. Cualquier error al comunicarse con la pasarela se registra en el log y la sesión
     * quedará hasta que expire por cuenta propia.
     *
     * @param pago pago cuya sesión de cobro se invalida, puede ser null si la reserva no tiene pago
     * @param ahora fecha y hora a usar como momento de expiración
     */
    private void cancelarCheckout(Pago pago, LocalDateTime ahora) {
        if (pago == null) return;
        try{
            getEstrategiaPago(pago).cancelarCheckout(pago, ahora);
        } catch (Exception e) {
            log.warn("No se pudo invalidar la sesión de cobro del pago {}. Quedará hasta expirar sola.", pago.getId(), e);
        }
    }

    private EstrategiaPago getEstrategiaPago(Pago pago){
        return estrategiaPagoFactory.get(pago.getMetodoPago());
    }

    /**
     * Cambia el estado de una única reserva y guarda en su propia transacción, para que un fallo al guardar
     * una reserva no revierta cambios de otras ya confirmadas (expiraciones y pagadas).
     *
     * @param r reserva a modificar
     * @param estadoReserva nuevo estado a asignarle a la reserva
     * @param ahora fecha y hora en la que se produce el cambio de estado
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void cambiarEstadoReservaYGuardar(Reserva r, EstadoReserva estadoReserva, LocalDateTime ahora){
        r.cambiarEstado(estadoReserva, ahora);
        reservaRepository.save(r);
    }

    private Usuario getUsuario(String emailUsuario){
        return usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("Usuario no encontrado"));
    }

    private Visitante getVisitante(Usuario usuario){
        return visitanteRepository.findByUsuario(usuario).orElseThrow(IllegalStateException::new);
    }

    private EstadoReserva getEstadoReserva(EstadoReservaNombre estadoReservaNombre){
        return reservaRepository.findEstadoReservaByEstadoReservaNombre(estadoReservaNombre)
                .orElseThrow(() -> new EstadoReservaNotFoundException(estadoReservaNombre));
    }

    private EstadoPago getEstadoPago(EstadoPagoNombre estadoPagoNombre){
        return estadoPagoRepository.findByNombre(estadoPagoNombre)
                .orElseThrow(() -> new EstadoPagoNotFoundException(estadoPagoNombre));
    }

    private Reserva getReserva(String idReserva, Visitante visitante){
        // Obtenemos la reserva, si no existe error.
        Reserva reserva = reservaRepository.findById(UUID.fromString(idReserva))
                .orElseThrow(ReservaNotFoundException::new);

        // Verificar que la reserva sea del usuario. Si no lo es, NOT FOUND para evitar dar información a no autorizados
        if (!reserva.getVisitante().getId().equals(visitante.getId()))
            throw new ReservaNotFoundException();

        return reserva;
    }

    private EstadoReembolso getEstadoReembolso(EstadoReembolsoNombre estadoReembolsoNombre){
        return estadoReembolsoRepository.findByNombre(estadoReembolsoNombre)
                .orElseThrow(() -> new EstadoReembolsoNotFoundException(estadoReembolsoNombre));
    }
}
