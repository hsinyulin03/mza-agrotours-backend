package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.config.RutasNotificacionesFront;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaReembolso;
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
import com.mza_agrotours.backend.exceptions.pago.*;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.util.*;

import static com.mza_agrotours.backend.enums.EstadoReservaNombre.*;

@Service
public class ReservaService {
    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    // Tiempo desde el pedido de un reembolso sin respuesta guardada hasta que se busca en la pasarela
    private static final Duration ESPERA_REEMBOLSO_SIN_CONFIRMAR = Duration.ofMinutes(3);

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
     * Workaround al todavía no tener notificaciones webhook de las pasarelas.
     * <p>
     * Tarea programada que busca todas las reservas "Pendiente" aún no expiradas y consulta, mediante la
     * estrategia del método de pago de cada una, si su pago fue aprobado.
     * <p>
     * La estrategia concilia el pago aprobado con la reserva (en MP, {@link com.mza_agrotours.backend.services.pago.ConciliadorPagoMP}:
     * monto, moneda, vendedor y comisiones). Si concilia, se guarda el ID de la transacción externa, se marca el pago
     * como "Aprobado", la reserva como "Pagada", se le quita la fecha de expiración y se invalida la sesión de cobro
     * para reducir la chance de un pago duplicado. Si no concilia, la reserva queda pendiente y se loguea como error
     * para revisión manual. Los errores de comunicación con la pasarela o de backend al procesar una
     * reserva particular no interrumpen el procesamiento del resto y quedan registrados en el log.
     * <p>
     * TODO (rama de reembolsos): solo se revisan reservas pendientes no expiradas. Un pago aprobado después de que la
     *  reserva expiró o se canceló (ej. baja de la actividad) nunca se detecta y el visitante pierde el dinero sin reserva.
     *  Agregar una revisión de reservas expiradas/canceladas con preference de MP que reembolse esos pagos automáticamente.
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
                // La estrategia concilia el pago con la reserva antes de devolverlo como aprobado
                ResultadoConsultaPagoDTO resultado = getEstrategiaPago(pago).consultarPago(r);
                if (!resultado.aprobado()) continue;

                pago.setIdTransaccionExterna(resultado.idTransaccionExterna()); // Necesario para reembolsar
                pago.cambiarEstado(estadoPago, ahora);  //Estado del pago

                r.setFechaHoraExpiracion(null);         // FHExpiración de la reserva
                self.cambiarEstadoReservaYGuardar(r, estadoReserva, ahora); // Estado de la reserva

                // Invalidar la sesión de cobro (para menor chance que se pague 2 veces)
                cancelarCheckout(pago, ahora);

                pagosExitosos++; // Contador de reservas pagadas para el log
            } catch (PagoNoConciliableException e) {
                // No se confirma: queda pendiente (y se reintenta) hasta que expire. Requiere revisión manual
                // TODO (rama de reembolsos): si no concilia por monto o moneda, reembolsar automáticamente (token del vendedor,
                //  X-Idempotency-Key "refund-<paymentId>", registrar el reembolso para no repetirlo y notificar al visitante).
                //  Si es por vendedor distinto NO reembolsar: solo alertar (indica un problema de vinculación o de datos).
                //  Para distinguir el motivo, PagoNoConciliableException necesita exponerlo (ej. un enum)
                log.error("Pago aprobado de la reserva {} que no concilia, requiere revisión manual: {}", r.getId(), e.getMessage());
                idFallidas.add(r.getId().toString());
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

    /**
     * Workaround al todavía no tener notificaciones webhook de las pasarelas.
     * <p>
     * Tarea programada que busca todos los reembolsos "En proceso" y consulta, mediante la estrategia del
     * método de pago de cada uno, si la pasarela ya lo resolvió.
     * <p>
     * Si fue aprobado, el reembolso pasa a "Reembolsado por productor", el pago a "Reembolsado" y la reserva
     * a "Cancelada con reembolso". Si fue rechazado, el reembolso pasa a "Pedido" para que lo haga el productor
     * y la reserva queda como está. Si sigue en proceso, se vuelve a consultar en la próxima ejecución.
     * <p>
     * Los reembolsos sin ID externo son pedidos cuya respuesta no se pudo guardar, así que no se sabe si la
     * pasarela los hizo. Pasado {@link #ESPERA_REEMBOLSO_SIN_CONFIRMAR} (para no pisar un pedido todavía en curso),
     * se busca el reembolso en la pasarela: si existe se sigue con él, si no pasa a "Pedido". Nunca se vuelve
     * a pedir el reembolso, para no hacerlo dos veces.
     * <p>
     * Los errores al procesar un reembolso particular no interrumpen el resto y quedan registrados en el log.
     */
    @Transactional(readOnly = true)
    public void confirmarReembolsos(){
        LocalDateTime ahora = LocalDateTime.now();
        List<Reembolso> reembolsos = reembolsoRepository.findReembolsosEnProceso();

        List<String> idFallidos = new ArrayList<>(); // Array de las id de reembolso que fallaron al procesarse
        int reembolsosResueltos = 0; // Cantidad de reembolsos aprobados o rechazados

        for (Reembolso r : reembolsos){
            Pago pago = r.getReserva().getPago();

            try{
                // Pedido sin respuesta guardada: buscar si la pasarela lo hizo
                if (r.getIdReembolsoExterno() == null){
                    if (r.getFechaHoraPedido().isAfter(ahora.minus(ESPERA_REEMBOLSO_SIN_CONFIRMAR))) continue;

                    Optional<String> idExterno = getEstrategiaPago(pago).buscarReembolso(pago);
                    if (idExterno.isEmpty()){
                        r.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.PEDIDO), ahora); // TODO: notificar al productor
                        self.guardarReembolso(r);
                        reembolsosResueltos++;
                        continue;
                    }
                    // Si sigue en proceso no se guarda el ID, se vuelve a buscar en la próxima ejecución
                    r.setIdReembolsoExterno(idExterno.get());
                }

                ResultadoConsultaReembolso resultado = getEstrategiaPago(pago).consultarReembolso(pago, r.getIdReembolsoExterno());

                switch (resultado){
                    case APROBADO -> completarReembolso(r, ahora);
                    case RECHAZADO -> r.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.PEDIDO), ahora); // TODO: notificar al productor
                    case EN_PROCESO -> { continue; }
                }

                self.guardarReembolso(r);
                reembolsosResueltos++;
            } catch (PasarelaPagoException e) {
                log.warn("Error de la pasarela consultando el reembolso {}", r.getId(), e);
                idFallidos.add(r.getId().toString());
            }
            catch (Exception e) {
                log.warn("Error de backend confirmando el reembolso {}", r.getId(), e);
                idFallidos.add(r.getId().toString());
            }
        }

        log.info("Se resolvieron {}/{} reembolsos en proceso. Los reembolsos con cambio fallido fueron {}, con ids: {}",
                reembolsosResueltos, reembolsos.size(), idFallidos.size(), idFallidos);
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
                    null,
                    RutasNotificacionesFront.detalleReserva(r.getId()),
                    actividad.getNombre(), r.getActividadDia().getFechaHoraInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
        log.info("Se cancelaron {} reservas pendientes por baja de la actividad {}", pendientes.size(), actividad.getId());
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
     * red o falla al guardar la respuesta), queda "En proceso" sin ID externo y {@link #confirmarReembolsos()}
     * lo verifica contra MP.
     */
    public IniciarReembolsoDTO handleCancelarReserva(String reservaId, String emailUsuario){
        // Paso 1: guardar la cancelación y el reembolso, antes de mover dinero
        Reembolso reembolso = self.cancelarReservaYCrearReembolso(reservaId, emailUsuario);

        if (reembolso == null) // Si no se devolvió reembolso es porque era un caso de no reembolso
            return new IniciarReembolsoDTO(ResultadoCancelacion.CANCELADA_SIN_REEMBOLSO);

        Pago pago = reembolso.getReserva().getPago();
        if (pago.getMetodoPago() == MetodoPago.MANUAL) // El reembolso manual se aprueba instantáneamente y queda completado
            return new IniciarReembolsoDTO(ResultadoCancelacion.REEMBOLSO_REALIZADO);

        // Paso 2: pedir el reembolso a la pasarela
        ResultadoReembolsoDTO resultado;
        try {
            resultado = getEstrategiaPago(pago).reembolsar(pago);
        } catch (PasarelaPagoException e) {
            log.warn("No se sabe si se realizó el reembolso {}, lo verificará la tarea programada", reembolso.getId(), e);
            return new IniciarReembolsoDTO(ResultadoCancelacion.REEMBOLSO_EN_PROCESO); // Se resuelve solo, avisamos cuando sepamos
        }

        // Paso 3: guardar la respuesta de la pasarela
        try {
            self.registrarPedidoReembolso(reembolso.getId(), resultado);
        } catch (Exception e) {
            log.error("No se pudo guardar la respuesta de MP del reembolso {}, lo verificará la tarea programada", reembolso.getId(), e);
        }

        if (resultado.aceptado())
            return new IniciarReembolsoDTO(ResultadoCancelacion.REEMBOLSO_EN_PROCESO); // Avisar que el proceso marcha bien, avisamos cuando sepamos
        return new IniciarReembolsoDTO(ResultadoCancelacion.REEMBOLSO_MANUAL_PRODUCTOR); // Avisar que hubo un error. Se notificó al propietario
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

    /**
     * Guarda un reembolso junto con su reserva (y el pago de ésta) en su propia transacción, para que un fallo
     * al guardar un reembolso no revierta los ya confirmados.
     *
     * @param reembolso reembolso a guardar
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void guardarReembolso(Reembolso reembolso){
        reservaRepository.save(reembolso.getReserva());
        reembolsoRepository.save(reembolso);
    }

    /**
     * Paso 1 de {@link #handleCancelarReserva}: valida y cancela la reserva, y guarda su reembolso si corresponde.
     * Si es por Mercado Pago, el reembolso queda "En proceso" sin ID externo y la reserva "Cancelada con reembolso
     * pendiente". Si es manual, el reembolso queda completado.
     *
     * @return el reembolso creado, o null si no corresponde reembolso
     */
    @Transactional
    protected Reembolso cancelarReservaYCrearReembolso(String reservaId, String emailUsuario){
        LocalDateTime ahora = LocalDateTime.now();

        Usuario usuario = getUsuario(emailUsuario);
        Visitante visitante = getVisitante(usuario);

        // Obtenemos la reserva, si no existe o no es del visitante, error.
        Reserva reserva = getReserva(reservaId, visitante);

        // TODO: notificar al productor y al visitante

        // CASOS DE NO REEMBOLSO
        if (!correspondeReembolso(reserva, ahora)){
            reserva.cambiarEstado(getEstadoReserva(CANCELADA_SIN_REEMBOLSO), ahora);
            reservaRepository.save(reserva);
            return null;
        }

        // CASOS DE REEMBOLSO
        Pago pago = reserva.getPago();

        Reembolso reembolso = new Reembolso();
        reembolso.setMontoReembolso(pago.getMontoTotal());
        reembolso.setFechaHoraPedido(ahora);
        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.EN_PROCESO), ahora);
        reembolso.setReserva(reserva);

        if (pago.getMetodoPago() == MetodoPago.MANUAL){
            // Pago manual, reembolso manual: se da por realizado en el momento
            completarReembolso(reembolso, ahora);
        } else {
            // Queda pendiente hasta que se confirme el reembolso en la pasarela
            reserva.cambiarEstado(getEstadoReserva(CANCELADA_REEMBOLSO_PENDIENTE), ahora);
        }

        reservaRepository.save(reserva);
        reembolsoRepository.save(reembolso);
        return reembolso;
    }

    /**
     * Paso 3 de {@link #handleCancelarReserva}: guarda la respuesta de la pasarela al pedido de reembolso.
     * Si fue aceptado se guarda su ID externo (sigue "En proceso" hasta que se confirme); si fue rechazado
     * pasa a "Pedido" para que lo haga el productor.
     */
    @Transactional
    protected void registrarPedidoReembolso(UUID reembolsoId, ResultadoReembolsoDTO resultado){
        Reembolso reembolso = reembolsoRepository.findById(reembolsoId).orElseThrow();

        if (resultado.aceptado())
            reembolso.setIdReembolsoExterno(resultado.idReembolsoExterno());
        else
            reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.PEDIDO), LocalDateTime.now()); // TODO: notificar al productor

        reembolsoRepository.save(reembolso);
    }

    /**
     * Da por realizado un reembolso: el reembolso pasa a "Reembolsado por productor" con su fecha de reembolso,
     * el pago a "Reembolsado" y la reserva a "Cancelada con reembolso". No guarda.
     *
     * @param reembolso reembolso realizado, con su reserva y pago
     * @param ahora fecha y hora en la que se realizó el reembolso
     */
    private void completarReembolso(Reembolso reembolso, LocalDateTime ahora){
        Reserva reserva = reembolso.getReserva();

        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR), ahora);
        reembolso.setFechaHoraReembolso(ahora);
        reserva.getPago().cambiarEstado(getEstadoPago(EstadoPagoNombre.REEMBOLSADO), ahora);
        reserva.cambiarEstado(getEstadoReserva(CANCELADA_CON_REEMBOLSO), ahora);
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

    private EstadoReembolso getEstadoReembolso(EstadoReembolsoNombre estadoReembolsoNombre){
        return estadoReembolsoRepository.findByNombre(estadoReembolsoNombre)
                .orElseThrow(() -> new EstadoReembolsoNotFoundException(estadoReembolsoNombre));
    }
}
