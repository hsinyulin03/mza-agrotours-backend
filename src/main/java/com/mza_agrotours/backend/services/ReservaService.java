package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.config.RutasNotificacionesFront;
import com.mza_agrotours.backend.dtos.archivo.DTOFotosResponse;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.reservas.*;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.TipoIdentificacionNombre;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.entities.reservas.ReservaDetalle;
import com.mza_agrotours.backend.enums.*;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.ReservaError;
import com.mza_agrotours.backend.exceptions.TipoIdentificacionInvalidoException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.exceptions.actividad.ActividadDiaNotFound;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotActiveException;
import com.mza_agrotours.backend.exceptions.actividad.ActividadNotFoundException;
import com.mza_agrotours.backend.exceptions.pago.*;
import com.mza_agrotours.backend.exceptions.reservas.*;
import com.mza_agrotours.backend.mappers.reserva.ReservaMapper;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.TipoIdentificacionRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import com.mza_agrotours.backend.repositories.VisitanteRepository;
import com.mza_agrotours.backend.repositories.actividad.ActividadRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import com.mza_agrotours.backend.services.notificaciones.ReservaNotificador;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

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
    private final ReservaNotificador reservaNotificador;
    private final ReembolsoService reembolsoService;
    private final ArchivoService archivoService;

    public ReservaService(ReservaRepository reservaRepository,
                          ReservaMapper reservaMapper,
                          ActividadRepository actividadRepository,
                          ParametrosService parametrosService,
                          UsuarioRepository usuarioRepository,
                          VisitanteRepository visitanteRepository,
                          TipoIdentificacionRepository tipoIdentificacionRepository,
                          EstrategiaPagoFactory estrategiaPagoFactory,
                          @Lazy ReservaService self,
                          EstadoPagoRepository estadoPagoRepository,
                          NotificacionService notificacionService,
                          ReservaNotificador reservaNotificador,
                          ReembolsoService reembolsoService,
                          ArchivoService archivoService) {
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
        this.reservaNotificador = reservaNotificador;
        this.reembolsoService = reembolsoService;
        this.archivoService = archivoService;
    }

    @Transactional
    public ConsultarReservaDTO getConsultarReserva(String idReserva, String emailUsuario){

        // Gettear al usuario y visitante
        Usuario usuario = getUsuario(emailUsuario);

        Visitante visitante = getVisitante(usuario);

        // Obtenemos la reserva, si no existe o no es del visitante, error.
        Reserva reserva = getReserva(idReserva, visitante);

        // Armamos el DTO
        return reservaMapper.reservaToConsultarReservaDTO(reserva, fotoPortada(reserva));
    }

    @Transactional(readOnly = true)
    public List<ListarReservaDTO> getListarReservas(String emailUsuario){

        List<ListarReservaDTO> dtos = new ArrayList<>();

        // Gettear al usuario y visitante
        Usuario usuario = getUsuario(emailUsuario);

        Visitante visitante = getVisitante(usuario);

        // Obtenemos las reservas (sin las expiradas, que confunden más de lo que informan). Si está vacío devolvemos el array vacío
        List<Reserva> reservas = reservaRepository.findNoExpiradasByVisitanteId(visitante.getId());
        if (reservas.isEmpty())
            return dtos;

        // Armamos el dto para cada reserva
        for (Reserva reserva: reservas){
            dtos.add(reservaMapper.reservaToListarReservaDTO(reserva, fotoPortada(reserva)));
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
                || !actividad.getEstablecimiento().admiteReservas(fechaHoraActual))
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
                throw new AppException(ReservaError.ACTIVIDAD_FULL);

        // Crear las nuevas ReservaDetalles y asignarle el estado
        List<ReservaDetalle> reservaDetalles = new ArrayList<>();
        List<RealizarReservaDetalleDTO> dtoDetalles = realizarReservaDTO.reservaDetalleList();
        Integer renglonReserva = 0;
        BigDecimal totalReserva = BigDecimal.valueOf(0);
        for (RealizarReservaDetalleDTO dtoDetalle: dtoDetalles){
            renglonReserva++;

            TipoIdentificacion tipoIdentificacion = tipoIdentificacionRepository.findByNombre(TipoIdentificacionNombre.valueOf(dtoDetalle.tipoIdentificacion()))
                    .orElseThrow(() -> new TipoIdentificacionInvalidoException("El tipo de identificación provisto no es válido"));

            int edad = dtoDetalle.fechaNacimiento().until(fechaHoraActual.toLocalDate())
                    .getYears();
            ActividadRangoEtario actividadRangoEtario = ares.stream()
                    .filter(are -> are.getEdadMinima() <= edad && are.getEdadMaxima() >= edad)
                    .findFirst().orElse(null);

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
        boolean pagada = pago.getEstadoActual().getEstadoPago().getNombre() == EstadoPagoNombre.APROBADO;
        LocalDateTime plazoPago = nuevaReserva.getFechaHoraExpiracion();
        if (pagada) {
            // Cambiar estado
            EstadoReserva estadoPagada = reservaRepository.findEstadoReservaByEstadoReservaNombre(PAGADA)
                    .orElseThrow(() -> new EstadoReservaNotFoundException(EstadoReservaNombre.PAGADA));
            nuevaReserva.cambiarEstado(estadoPagada, fechaHoraActual);

            // Eliminar la fecha de expiración
            nuevaReserva.setFechaHoraExpiracion(null);
        }

        reservaRepository.save(nuevaReserva);

        // Avisar al visitante: la reserva confirmada o el plazo que tiene para pagarla. A los productores solo la confirmada
        if (pagada) {
            reservaNotificador.notificarVisitante(nuevaReserva, TipoNotificacionNombre.RESERVA_PAGADA, pago.getMontoTotal().toPlainString());
            reservaNotificador.notificarProductoresReservaConfirmada(nuevaReserva);
        } else
            reservaNotificador.notificarVisitante(nuevaReserva, TipoNotificacionNombre.RESERVA_INICIADA, plazoPago.format(ReservaNotificador.FORMATO_FECHA_HORA));

        // Avisar al frontend de qué pasó
        return new IniciarReservaDTO(reservaMapper.reservaToConsultarReservaDTO(nuevaReserva, fotoPortada(nuevaReserva)), preferenceID);
    }

    /**
     * Tarea programada que busca todas las reservas "Pendiente" cuya fecha y hora de expiración ya pasó,
     * las marca como "Expirada", les quita la fecha de expiración para que no sean revisadas nuevamente y avisa
     * al visitante.
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
                self.expirarReservaYNotificar(r, estadoReserva, ahora);
            } catch (ReservaNoPendienteException e) {
                log.info("La reserva {} se pagó o canceló antes de expirar", r.getId());
            } catch (Exception e) {
                idFallidas.add(r.getId().toString());
            }
        }

        log.info("Se expiraron {}/{} reservas. Las reservas no expiradas fueron {}, con ids: {}",
                reservas.size() - idFallidas.size(), reservas.size(), idFallidas.size(), idFallidas);
    }

    /**
     * Tarea programada que busca todas las reservas "Pagada" cuyo día de actividad ya está "Finalizada"
     * y las marca como "Finalizada".
     * La búsqueda no depende de qué días se finalizaron en la última corrida, por lo que también alcanza a reservas
     * pagadas tarde (luego de finalizado el día) o que fallaron en una corrida anterior.
     * Cada reserva se cambia y guarda en su propia transacción, por lo que el fallo de una no afecta a las demás.
     * Las reservas que no pudieron finalizarse quedan registradas en el log para su seguimiento.
     */
    @Transactional(readOnly = true)
    public void finalizarReservas(){
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservas = reservaRepository.findReservasPagadasDeDiasFinalizados();
        if (reservas.isEmpty()) return;

        EstadoReserva estadoReserva = getEstadoReserva(FINALIZADA);

        List<String> idFallidas = new ArrayList<>();

        for (Reserva r : reservas){
            try{
                self.finalizarReservaYsolicitarValoracion(r, estadoReserva, ahora);
            } catch (Exception e) {
                log.warn("Error de backend finalizando reserva {}", r.getId(), e);
                idFallidas.add(r.getId().toString());
            }
        }

        log.info("Se finalizaron {}/{} reservas. Las reservas no finalizadas fueron {}, con ids: {}",
                reservas.size() - idFallidas.size(), reservas.size(), idFallidas.size(), idFallidas);
    }

    /**
     * Tarea programada que busca todas las reservas "Pendiente" y consulta, mediante la estrategia del método de pago
     * de cada una, si su pago fue aprobado. Se saltean las de medios que confirman sus pagos por notificación
     * (ver {@link #confirmarPagoNotificado}), como Mercado Pago con webhook configurado.
     * <p>
     * Cada pago aprobado se confirma con {@link #aplicarResultadoPago}. Los errores de comunicación con la pasarela o
     * de backend al procesar una reserva particular no interrumpen el procesamiento del resto y quedan registrados
     * en el log.
     * <p>
     * NOTE: gap conocido, solo se revisan reservas pendientes. Un pago aprobado después de que la reserva
     *  expiró o se canceló (ej. baja de la actividad) no se detecta ni se reembolsa automáticamente.
     */
    @Transactional(readOnly = true)
    public void pagarReservas(){
        LocalDateTime ahora = LocalDateTime.now();
        List<Reserva> reservas = reservaRepository.findReservasPendientes(ahora);

        EstadoReserva estadoReserva = getEstadoReserva(PAGADA);
        EstadoPago estadoPago = getEstadoPago(EstadoPagoNombre.APROBADO);

        List<String> idFallidas = new ArrayList<>(); // Array de las id de reserva que fallaron en pasarse a pagada
        int consultadas = 0;
        int pagosExitosos = 0; // Cantidad de pagos exitosos
        int pagosReembolsados = 0; // Cantidad de pagos que no conciliaron y se reembolsaron

        for (Reserva r : reservas){
            Pago pago = r.getPago();

            try{
                EstrategiaPago estrategia = getEstrategiaPago(pago);
                if (!estrategia.requiereConsultaPeriodica()) continue;
                consultadas++;

                switch (aplicarResultadoPago(r, () -> estrategia.consultarPago(r), estadoReserva, estadoPago, ahora)) {
                    case PAGADA -> pagosExitosos++;
                    case REEMBOLSADA -> pagosReembolsados++;
                    default -> { }
                }
            } catch (PasarelaPagoException e) {
                log.warn("Error de la pasarela consultando el pago de la reserva {}", r.getId(), e);
                idFallidas.add(r.getId().toString());
            }
            catch (Exception e) {
                log.warn("Error de backend checkeando reservas pagas {}", r.getId(), e);
                idFallidas.add(r.getId().toString());
            }
        }

        log.info("Se pagaron {}/{} reservas pendientes consultadas y se reembolsaron {} pagos que no conciliaron. Las reservas con cambio fallido fueron {}, con ids: {}",
                pagosExitosos, consultadas, pagosReembolsados, idFallidas.size(), idFallidas);
    }

    /**
     * Confirma el pago de una reserva a partir de una notificación de la pasarela (webhook). La notificación solo
     * aporta el ID de la transacción: la estrategia la consulta a la pasarela y valida que sea de esta reserva.
     * <p>
     * Es idempotente: si la reserva no existe o ya no está "Pendiente" (pagada por una notificación anterior,
     * expirada o cancelada) no se hace nada, así que la pasarela puede repetir la notificación sin efectos.
     *
     * @param reservaId reserva a la que corresponde la notificación
     * @param idTransaccionExterna ID de la transacción notificada
     * @throws PasarelaPagoException si falla la comunicación con la pasarela; la notificación debe reintentarse
     */
    @Transactional(readOnly = true)
    public void confirmarPagoNotificado(UUID reservaId, String idTransaccionExterna){
        Optional<Reserva> optReserva = reservaRepository.findById(reservaId);
        if (optReserva.isEmpty()) {
            log.warn("Notificación de pago {} para la reserva inexistente {}", idTransaccionExterna, reservaId);
            return;
        }
        Reserva r = optReserva.get();
        Pago pago = r.getPago();
        // NOTE: gap conocido, igual que en el polling: un pago aprobado de una reserva ya expirada o cancelada no se reembolsa
        if (pago == null || r.getEstadoActual().getEstadoReserva().getNombre() != PENDIENTE) {
            log.info("Notificación de pago {} para la reserva {}, que ya no está pendiente: se ignora", idTransaccionExterna, reservaId);
            return;
        }

        ResultadoAplicacionPago resultado = aplicarResultadoPago(r,
                () -> getEstrategiaPago(pago).consultarPagoNotificado(r, idTransaccionExterna),
                getEstadoReserva(PAGADA), getEstadoPago(EstadoPagoNombre.APROBADO), LocalDateTime.now());
        log.info("Notificación de pago {} para la reserva {}: {}", idTransaccionExterna, reservaId, resultado);
    }

    private enum ResultadoAplicacionPago { NO_APROBADO, PAGADA, REEMBOLSADA, YA_NO_PENDIENTE }

    /**
     * Consulta el pago de una reserva pendiente y aplica el resultado. La estrategia concilia el pago aprobado con la
     * reserva (en MP, {@link com.mza_agrotours.backend.services.pago.ConciliadorPagoMP}: monto, moneda, vendedor y
     * comisiones). Si concilia, se guarda el ID de la transacción externa, se marca el pago como "Aprobado", la reserva
     * como "Pagada", se le quita la fecha de expiración, se avisa al visitante y se invalida la sesión de cobro para
     * reducir la chance de un pago duplicado. Si no concilia, el pago se reembolsa (ver {@link #reembolsarPagoNoConciliable}).
     * Si otro proceso sacó a la reserva de "Pendiente" mientras tanto, no se aplica nada.
     *
     * @throws PasarelaPagoException si falla la comunicación con la pasarela
     */
    private ResultadoAplicacionPago aplicarResultadoPago(Reserva r, Supplier<ResultadoConsultaPagoDTO> consulta,
                                                        EstadoReserva estadoPagada, EstadoPago estadoAprobado, LocalDateTime ahora){
        try {
            return confirmarOReembolsar(r, consulta, estadoPagada, estadoAprobado, ahora);
        } catch (ReservaNoPendienteException e) {
            log.info("La reserva {} dejó de estar pendiente mientras se confirmaba su pago", r.getId());
            return ResultadoAplicacionPago.YA_NO_PENDIENTE;
        }
    }

    private ResultadoAplicacionPago confirmarOReembolsar(Reserva r, Supplier<ResultadoConsultaPagoDTO> consulta,
                                                         EstadoReserva estadoPagada, EstadoPago estadoAprobado, LocalDateTime ahora){
        Pago pago = r.getPago();
        try {
            ResultadoConsultaPagoDTO resultado = consulta.get();
            if (!resultado.aprobado()) return ResultadoAplicacionPago.NO_APROBADO;

            pago.setIdTransaccionExterna(resultado.idTransaccionExterna()); // Necesario para reembolsar
            pago.cambiarEstado(estadoAprobado, ahora);  //Estado del pago

            r.setFechaHoraExpiracion(null);         // FHExpiración de la reserva
            self.confirmarPagoReservaYNotificar(r, estadoPagada, ahora); // Estado de la reserva y aviso al visitante

            // Invalidar la sesión de cobro (para menor chance que se pague 2 veces)
            cancelarCheckout(pago, ahora);
            return ResultadoAplicacionPago.PAGADA;
        } catch (PagoNoConciliableException e) {
            // NOTE: se reembolsa siempre, incluso si se acreditó a otro vendedor (gap conocido: ese reembolso
            //  probablemente falle con el token del vendedor y termine "Pedido")
            log.error("Pago aprobado de la reserva {} que no concilia, se reembolsa: {}", r.getId(), e.getMessage());
            // Si falla antes de guardar el reembolso, la reserva sigue pendiente y se reintenta
            reembolsarPagoNoConciliable(r, e, ahora);
            return ResultadoAplicacionPago.REEMBOLSADA;
        }
    }

    /**
     * Reembolsa un pago aprobado que no concilia con su reserva, con los mismos tres pasos que una cancelación
     * para que ningún fallo termine en un doble reembolso: primero se guarda la reserva como "Reembolsada por pago
     * no válido" y el reembolso "En proceso" sin ID externo, después se pide el reembolso a la pasarela, y por último
     * se guarda su respuesta. Además se invalida la sesión de cobro para que no se pague de nuevo.
     *
     * @param reserva reserva pendiente cuyo pago no concilia
     * @param e excepción de la conciliación, con el ID y el monto del pago a reembolsar
     * @param ahora fecha y hora del proceso
     */
    private void reembolsarPagoNoConciliable(Reserva reserva, PagoNoConciliableException e, LocalDateTime ahora){
        Pago pago = reserva.getPago();
        BigDecimal montoPagado = e.getMontoPagado() != null ? e.getMontoPagado() : pago.getMontoTotal();

        // Paso 1: guardar la reserva y el reembolso, antes de mover dinero
        Reembolso reembolso = self.registrarPagoNoConciliable(reserva, e.getIdTransaccionExterna(), montoPagado, ahora);

        // Invalidar la sesión de cobro para que no se vuelva a pagar la reserva
        cancelarCheckout(pago, ahora);

        // Pasos 2 y 3: pedir el reembolso y guardar la respuesta
        reembolsoService.pedirReembolso(reembolso);
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

        for (Reserva r : pendientes) {
            cancelarReservaCancelable(r, ahora);
            reservaNotificador.notificarVisitante(r, TipoNotificacionNombre.RESERVA_CANCELADA_POR_BAJA_ACTIVIDAD);
        }
        log.info("Se cancelaron {} reservas pendientes por baja de la actividad {}", pendientes.size(), actividad.getId());
    }

    @Transactional
    public void cancelarReservasPorBajaDeActividadDia(ActividadDia actividadDia, LocalDateTime ahora) {
        List<Reserva> reservasNoFinalizadasNiCanceladas = this.reservaRepository.findNoFinalizadosNiCanceladosByActividadDiaId(actividadDia.getId());
        if (reservasNoFinalizadasNiCanceladas.isEmpty()) return;

        // El reembolso lo pide ReembolsoService#confirmarReembolsos después del commit. Al productor no se le avisa: canceló él
        for (Reserva reserva : reservasNoFinalizadasNiCanceladas) {
            Reembolso reembolso = cancelarReservaCancelable(reserva, ahora);
            if (reembolso == null)
                reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.RESERVA_CANCELADA_POR_DIA_CANCELADO);
            else
                reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.RESERVA_CANCELADA_POR_DIA_CANCELADO_REEMBOLSO,
                        reembolso.getMontoReembolso().toPlainString());
        }
        log.info("Se cancelaron {} reservas por la cancelación del día {}", reservasNoFinalizadasNiCanceladas.size(), actividadDia.getId());
    }

    // El día ya tiene la fecha nueva: el notificador la pone sola, la anterior va como dato
    @Transactional
    public void notificarReprogramacionDeActividadDia(ActividadDia actividadDia, LocalDateTime fechaHoraInicioAnterior, String motivo) {
        List<Reserva> reservasPagadas = reservaRepository.findPagadasByActividadDiaId(actividadDia.getId());
        String fechaAnterior = fechaHoraInicioAnterior.format(ReservaNotificador.FORMATO_FECHA_HORA);
        for (Reserva reserva : reservasPagadas) {
            reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.RESERVA_REPROGRAMADA, fechaAnterior, motivo);
        }
    }

    @Transactional(readOnly = true)
    public IniciarReembolsoDTO handleCancelarReservaCondicion(String reservaId, String emailUsuario){
        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = getUsuario(emailUsuario);
        Visitante visitante = getVisitante(usuario);

        // Obtenemos la reserva, si no existe o no es del visitante, error.
        Reserva reserva = getReserva(reservaId, visitante);

        if (correspondeReembolso(reserva, ahora))
            return new IniciarReembolsoDTO(ResultadoCancelacion.CANCELACION_CON_REEMBOLSO);

        return new IniciarReembolsoDTO(ResultadoCancelacion.CANCELACION_SIN_REEMBOLSO);
    }

    /**
     * Cancela una reserva "Pagada" a pedido del visitante, con o sin reembolso según corresponda.
     * <p>
     * Si el reembolso va por Mercado Pago, se hace en tres pasos para que ningún fallo termine en un doble
     * reembolso: primero se guarda la cancelación y el reembolso "En proceso" sin ID externo, después se pide
     * el reembolso a la pasarela, y por último se guarda su respuesta. Nunca se reintenta el pedido: si MP lo
     * rechaza, el reembolso pasa a "Pedido" para que lo haga el productor. Si no se sabe si MP lo hizo (error de
     * red o falla al guardar la respuesta), queda "En proceso" sin ID externo y {@link ReembolsoService#confirmarReembolsos()}
     * lo verifica contra MP.
     */
    public IniciarReembolsoDTO handleCancelarReserva(String reservaId, String emailUsuario){
        // Paso 1: guardar la cancelación y el reembolso, antes de mover dinero
        Reembolso reembolso = self.cancelarPorVisitante(reservaId, emailUsuario);

        if (reembolso == null)
            return new IniciarReembolsoDTO(ResultadoCancelacion.CANCELADA_SIN_REEMBOLSO);

        // Pasos 2 y 3: pedir el reembolso a la pasarela y guardar su respuesta
        ResultadoCancelacion resultado = switch (reembolsoService.pedirReembolso(reembolso)) {
            case REEMBOLSADO_PRODUCTOR -> ResultadoCancelacion.REEMBOLSO_REALIZADO;
            case PEDIDO -> ResultadoCancelacion.REEMBOLSO_MANUAL_PRODUCTOR;
            default -> ResultadoCancelacion.REEMBOLSO_EN_PROCESO;
        };
        return new IniciarReembolsoDTO(resultado);
    }

    /**
     * Paso 1 de {@link #handleCancelarReserva}: valida que la reserva sea del visitante, decide si corresponde
     * reembolso, la cancela y avisa al visitante y a los productores.
     *
     * @return el reembolso "En proceso" creado, o null si no corresponde reembolso
     */
    @Transactional
    protected Reembolso cancelarPorVisitante(String reservaId, String emailUsuario){
        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = getUsuario(emailUsuario);
        Visitante visitante = getVisitante(usuario);
        Reserva reserva = getReserva(reservaId, visitante);

        Reembolso reembolso = cancelarReservaCancelable(reserva, correspondeReembolso(reserva, ahora), ahora);

        if (reembolso == null) {
            reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.RESERVA_CANCELADA_SIN_REEMBOLSO,
                    parametrosService.getInstance().getDiasMinReembolso());
            reservaNotificador.notificarProductores(reserva, PermisoCodigo.GESTIONAR_ACTIVIDAD, TipoNotificacionNombre.RESERVA_CANCELADA_PRODUCTOR,
                    reservaNotificador.enlaceReservasEstablecimiento(reserva), reserva.getReservaDetalles().size());
            return null;
        }

        String monto = reembolso.getMontoReembolso().toPlainString();
        reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.RESERVA_CANCELADA_REEMBOLSO_EN_PROCESO, monto);
        reservaNotificador.notificarProductores(reserva, PermisoCodigo.GESTIONAR_ACTIVIDAD, TipoNotificacionNombre.RESERVA_CANCELADA_REEMBOLSO_PRODUCTOR,
                reservaNotificador.enlaceReservasEstablecimiento(reserva), reserva.getReservaDetalles().size(), monto);
        return reembolso;
    }

    //Recordatorio: notifica a las reservas pagadas cuyo día empieza en las próximas 24 h (lo llama el scheduler)
    public void enviarRecordatoriosPendientes() {
        LocalDateTime ahora = LocalDateTime.now();
        List<UUID> idsPendientes = reservaRepository.findIdsRecordatorioPendiente(ahora, ahora.plusHours(24));
        if (idsPendientes.isEmpty()) {
            return;
        }

        List<String> idsFallidos = new ArrayList<>();
        for (UUID reservaId : idsPendientes) {
            try {
                self.enviarRecordatorio(reservaId, ahora);
            } catch (Exception e) {
                idsFallidos.add(reservaId.toString());
                log.warn("No se pudo enviar el recordatorio de la reserva {}", reservaId, e);
            }
        }

        log.info("Se enviaron {}/{} recordatorios de reserva. Fallaron {}, con ids: {}",
                idsPendientes.size() - idsFallidos.size(), idsPendientes.size(), idsFallidos.size(), idsFallidos);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enviarRecordatorio(UUID reservaId, LocalDateTime ahora) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(ReservaNotFoundException::new);

        notificacionService.crearNotificacion(
                reserva.getVisitante().getUsuario(),
                TipoNotificacionNombre.RECORDATORIO_RESERVA,
                null,
                RutasNotificacionesFront.detalleReserva(reserva.getId()),
                reserva.getActividadDia().getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                reserva.getActividadDia().getFechaHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")),
                reserva.getActividad().getNombre(),
                reserva.getActividad().getEstablecimiento().getNombre());

        reserva.setFechaHoraRecordatorio(ahora);
    }

    // cambiamos la reserva a estado finalizado y enviamos notificación al visitante solicitándole una valoración
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizarReservaYsolicitarValoracion(Reserva reserva, EstadoReserva estadoFinalizada, LocalDateTime ahora) {
        reserva.cambiarEstado(estadoFinalizada, ahora);
        reservaRepository.save(reserva);

        notificacionService.crearNotificacion(
                reserva.getVisitante().getUsuario(),
                TipoNotificacionNombre.VALORAR_ACTIVIDAD,
                null,
                RutasNotificacionesFront.valorarExperiencia(reserva.getId()),
                reserva.getActividad().getNombre(),
                reserva.getActividad().getEstablecimiento().getNombre(),
                reserva.getActividadDia().getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
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

    // El pago es obligatorio: solo cancelarCheckout admite reservas sin pago, y lo chequea antes de llamar acá
    private EstrategiaPago getEstrategiaPago(Pago pago){
        Objects.requireNonNull(pago, "Se necesita el pago para obtener su estrategia");
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

    /**
     * Como {@link #cambiarEstadoReservaYGuardar}, para una reserva que expiró por no pagarse a tiempo: además
     * avisa al visitante en la misma transacción, así solo se notifica si el cambio se guardó.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void expirarReservaYNotificar(Reserva r, EstadoReserva estadoExpirada, LocalDateTime ahora){
        bloquearPendiente(r);
        cambiarEstadoReservaYGuardar(r, estadoExpirada, ahora);
        reservaNotificador.notificarVisitante(r, TipoNotificacionNombre.RESERVA_EXPIRADA);
    }

    /**
     * Como {@link #cambiarEstadoReservaYGuardar}, para una reserva cuyo pago se confirmó: además avisa al
     * visitante y a los productores en la misma transacción, así solo se notifica si el cambio se guardó.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void confirmarPagoReservaYNotificar(Reserva r, EstadoReserva estadoPagada, LocalDateTime ahora){
        bloquearPendiente(r);
        cambiarEstadoReservaYGuardar(r, estadoPagada, ahora);
        reservaNotificador.notificarVisitante(r, TipoNotificacionNombre.RESERVA_PAGADA, r.getPago().getMontoTotal().toPlainString());
        reservaNotificador.notificarProductoresReservaConfirmada(r);
    }

    /**
     * Paso 1 de {@link #reembolsarPagoNoConciliable}: registra el pago que no concilia y su reembolso, y avisa al
     * visitante. El pago queda "Aprobado" con el ID de la transacción (necesario para reembolsar), la reserva
     * "Reembolsada por pago no válido" (libera el cupo) y el reembolso "En proceso" sin ID externo.
     *
     * @return el reembolso creado
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Reembolso registrarPagoNoConciliable(Reserva reserva, String idTransaccionExterna, BigDecimal montoPagado, LocalDateTime ahora){
        bloquearPendiente(reserva);
        Pago pago = reserva.getPago();
        pago.setIdTransaccionExterna(idTransaccionExterna);
        pago.cambiarEstado(getEstadoPago(EstadoPagoNombre.APROBADO), ahora); // La pasarela lo aprobó; pasa a "Reembolsado" al confirmarse

        reserva.setFechaHoraExpiracion(null);
        reserva.cambiarEstado(getEstadoReserva(REEMBOLSO_NO_CONCILIACION), ahora);

        reservaRepository.save(reserva);
        Reembolso reembolso = reembolsoService.crearReembolsoEnProceso(reserva, montoPagado, ahora);

        reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.RESERVA_REEMBOLSADA_PAGO_NO_VALIDO, montoPagado.toPlainString());

        return reembolso;
    }

    /**
     * Bloquea la fila de la reserva hasta el fin de la transacción y verifica que siga "Pendiente", para que el pago
     * por webhook, el polling y la expiración no apliquen dos cambios de estado sobre la misma reserva.
     *
     * @throws ReservaNoPendienteException si otro proceso ya la sacó de "Pendiente"
     */
    private void bloquearPendiente(Reserva r){
        Reserva actual = reservaRepository.findByIdForUpdate(r.getId()).orElseThrow(ReservaNotFoundException::new);
        if (actual.getEstadoActual().getEstadoReserva().getNombre() != PENDIENTE)
            throw new ReservaNoPendienteException(r.getId());
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

    private DTOFotosResponse fotoPortada(Reserva reserva) {
        return archivoService.mapearFoto(reserva.getActividad().archivoPortada());
    }

    private Reserva getReserva(String idReserva, Visitante visitante){
        // Un ID malo se trata igual que una reserva inexistente
        UUID uuid;
        try {
            uuid = UUID.fromString(idReserva);
        } catch (IllegalArgumentException e) {
            throw new ReservaNotFoundException();
        }

        // Obtenemos la reserva, si no existe error.
        Reserva reserva = reservaRepository.findById(uuid)
                .orElseThrow(ReservaNotFoundException::new);

        // Verificar que la reserva sea del usuario. Si no lo es, NOT FOUND para evitar dar información a no autorizados
        if (!reserva.getVisitante().getId().equals(visitante.getId()))
            throw new ReservaNotFoundException();

        return reserva;
    }

    /**
     * Valida que la reserva pueda cancelarse y decide si corresponde reembolso.
     * Corresponde si la actividad está a más de diasMinReembolso de ocurrir o fue reprogramada.
     *
     * @throws ReembolsoStateException si la reserva no está "Pagada"
     * @throws ReembolsoDateException si la actividad ya transcurrió
     */
    private boolean correspondeReembolso(Reserva reserva, LocalDateTime ahora){
        EstadoReservaNombre estadoReserva = reserva.getEstadoActual().getEstadoReserva().getNombre();
        if (estadoReserva != PAGADA)
            throw new ReembolsoStateException(estadoReserva.getEstado());

        ActividadDia actividadDia = reserva.getActividadDia();
        if (actividadDia.getFechaHoraFin().isBefore(ahora))
            throw new ReembolsoDateException(actividadDia.getFechaHoraFin().toString(), ahora.toString());

        Integer diasMinReembolso = parametrosService.getInstance().getDiasMinReembolso();
        boolean faltanDiasSuficientes = ahora.plusDays(diasMinReembolso).isBefore(actividadDia.getFechaHoraInicio());
        boolean reprogramada = actividadDia.getEstadoActual().getEstado().getNombre() == EstadoActividadDiaNombre.REPROGRAMADA;

        return faltanDiasSuficientes || reprogramada;
    }

    private Reembolso cancelarReservaCancelable(Reserva reserva, LocalDateTime ahora) {
        return cancelarReservaCancelable(reserva, true, ahora);
    }

    /**
     * Cancela una reserva "Pendiente" o "Pagada". La pendiente no se cobró: queda "Cancelada sin reembolso" y se
     * invalida su sesión de cobro. La pagada queda "Cancelada sin reembolso" si no corresponde reembolso, o
     * "Cancelada con reembolso pendiente" con un reembolso "En proceso" que se pide a la pasarela después del commit.
     * No avisa a nadie: los avisos dependen de quién cancela.
     *
     * @param correspondeReembolso si la política de quien cancela le da reembolso al visitante, en caso de haber pagado
     * @return el reembolso "En proceso" creado, o null si no hay reembolso
     * @throws ReembolsoStateException si la reserva no está "Pendiente" ni "Pagada"
     */
    private Reembolso cancelarReservaCancelable(Reserva reserva, boolean correspondeReembolso, LocalDateTime ahora) {
        EstadoReservaNombre estado = reserva.getEstadoActual().getEstadoReserva().getNombre();

        if (estado == PENDIENTE) {
            reserva.setFechaHoraExpiracion(null);
            reserva.cambiarEstado(getEstadoReserva(CANCELADA_SIN_REEMBOLSO), ahora);
            cancelarCheckout(reserva.getPago(), ahora);
            return null;
        }
        if (estado != PAGADA)
            throw new ReembolsoStateException(estado.getEstado());

        if (!correspondeReembolso) {
            reserva.cambiarEstado(getEstadoReserva(CANCELADA_SIN_REEMBOLSO), ahora);
            reservaRepository.save(reserva);
            return null;
        }

        reserva.cambiarEstado(getEstadoReserva(CANCELADA_REEMBOLSO_PENDIENTE), ahora);
        reservaRepository.save(reserva);
        return reembolsoService.crearReembolsoEnProceso(reserva, reserva.getPago().getMontoTotal(), ahora);
    }
}
