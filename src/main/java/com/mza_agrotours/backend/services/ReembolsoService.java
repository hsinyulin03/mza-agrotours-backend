package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.pago.*;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimiento;
import com.mza_agrotours.backend.entities.pago.*;
import com.mza_agrotours.backend.entities.reservas.EstadoReserva;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.*;
import com.mza_agrotours.backend.exceptions.AdministradorSistemasError;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.pago.*;
import com.mza_agrotours.backend.exceptions.reservas.EstadoReservaNotFoundException;
import com.mza_agrotours.backend.mappers.ReembolsoMapper;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.ReservaRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoRepository;
import com.mza_agrotours.backend.repositories.pago.ReembolsoSistemaPagoRepository;
import com.mza_agrotours.backend.services.notificaciones.ReservaNotificador;
import com.mza_agrotours.backend.services.pago.EstrategiaPago;
import com.mza_agrotours.backend.services.pago.EstrategiaPagoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.mza_agrotours.backend.enums.EstadoReservaNombre.CANCELADA_CON_REEMBOLSO;
import static com.mza_agrotours.backend.enums.EstadoReservaNombre.REEMBOLSO_NO_CONCILIACION;

@Service
public class ReembolsoService {
    private static final Logger log = LoggerFactory.getLogger(ReembolsoService.class);

    // Tiempo desde el pedido de un reembolso sin respuesta guardada hasta que se busca en la pasarela
    private static final Duration ESPERA_REEMBOLSO_SIN_CONFIRMAR = Duration.ofMinutes(3);

    // Reembolsos que tiene que ver el administrador: los que el productor no pagó y los que pagó el sistema
    private static final List<EstadoReembolsoNombre> ESTADOS_REEMBOLSO_ADMIN =
            List.of(EstadoReembolsoNombre.IMPAGO, EstadoReembolsoNombre.REEMBOLSADO_SISTEMA);

    private final ReembolsoService self;
    private final ReembolsoRepository reembolsoRepository;
    private final ReembolsoSistemaPagoRepository reembolsoSistemaPagoRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final EstadoReembolsoRepository estadoReembolsoRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final ReservaRepository reservaRepository;
    private final ReembolsoMapper reembolsoMapper;
    private final EstrategiaPagoFactory estrategiaPagoFactory;
    private final ParametrosService parametrosService;
    private final ReservaNotificador reservaNotificador;
    private final DeudaEstablecimientoService deudaEstablecimientoService;

    public ReembolsoService(ReembolsoRepository reembolsoRepository, ReembolsoSistemaPagoRepository reembolsoSistemaPagoRepository, EstadoPagoRepository estadoPagoRepository, EstadoReembolsoRepository estadoReembolsoRepository, AdministradorSistemasRepository administradorSistemasRepository, ReservaRepository reservaRepository, ReembolsoMapper reembolsoMapper, EstrategiaPagoFactory estrategiaPagoFactory, ParametrosService parametrosService, ReservaNotificador reservaNotificador, @Lazy ReembolsoService self, DeudaEstablecimientoService deudaEstablecimientoService) {
        this.self = self;
        this.reembolsoRepository = reembolsoRepository;
        this.reembolsoSistemaPagoRepository = reembolsoSistemaPagoRepository;
        this.estadoPagoRepository = estadoPagoRepository;
        this.estadoReembolsoRepository = estadoReembolsoRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.reservaRepository = reservaRepository;
        this.reembolsoMapper = reembolsoMapper;
        this.estrategiaPagoFactory = estrategiaPagoFactory;
        this.parametrosService = parametrosService;
        this.reservaNotificador = reservaNotificador;
        this.deudaEstablecimientoService = deudaEstablecimientoService;
    }

    @Transactional(readOnly = true)
    public Page<ReembolsoGetCatalogoDTO> listarReembolsosPendientes(UUID establecimientoId, Pageable pageable){
        Page<ReembolsoGetCatalogoDTO> pendientes = reembolsoRepository.findPedidosByEstablecimiento(establecimientoId, pageable);
        return pendientes;
    }

    /**
     * Lista los reembolsos que el productor no pagó (IMPAGO) y los que terminó pagando un administrador
     * (REEMBOLSADO_SISTEMA), del que más tiempo lleva en ese estado al más reciente.
     */
    @Transactional(readOnly = true)
    public List<ListarReembolsoAdminDTO> listarReembolsosImpagos(){
        return reembolsoRepository.findByEstadoActualIn(ESTADOS_REEMBOLSO_ADMIN)
                .stream()
                .map(reembolsoMapper::reembolsoToListarReembolsoAdminDTO)
                .toList();
    }

    /**
     * Detalle de un reembolso del listado de impagos, con los datos de la reserva y los contactos del visitante
     * y del productor líder, para que el administrador pueda resolverlo.
     * Solo se pueden consultar los reembolsos IMPAGO o REEMBOLSADO_SISTEMA.
     */
    @Transactional(readOnly = true)
    public DetalleReembolsoAdminDTO getDetalleReembolsoAdmin(UUID idReembolso){
        Reembolso reembolso = reembolsoRepository.findByIdAndEstadoActualIn(idReembolso, ESTADOS_REEMBOLSO_ADMIN)
                .orElseThrow(ReembolsoNotFoundException::new);

        // Solo existe si el reembolso ya lo pagó el sistema
        ReembolsoSistemaPago reembolsoSistemaPago = reembolsoSistemaPagoRepository.findByReembolsoId(idReembolso)
                .orElse(null);

        return reembolsoMapper.reembolsoToDetalleReembolsoAdminDTO(reembolso, reembolsoSistemaPago);
    }

    /**
     * Registra el pago que hizo un administrador para cubrir un reembolso IMPAGO. Crea un pago manual aprobado por el
     * monto del reembolso, con la fecha y el ID externo de la transacción, y lo vincula al reembolso junto con el
     * administrador y el medio de pago real. El reembolso pasa a "Reembolsado por sistema", el pago original a
     * "Reembolsado" y, si es por una cancelación, la reserva a "Cancelada con reembolso". Avisa al visitante y a
     * los productores.
     *
     * @param idReembolso reembolso a pagar
     * @param dto datos de la transacción hecha por el administrador
     * @param emailAdmin email del administrador que la carga
     * @return el detalle del reembolso ya pagado
     * @throws AppException si el administrador no existe o no está activo
     * @throws ReembolsoNotFoundException si el reembolso no existe o no es IMPAGO ni REEMBOLSADO_SISTEMA
     * @throws ReembolsoNoImpagoException si el reembolso ya fue pagado por el sistema
     */
    @Transactional
    public DetalleReembolsoAdminDTO registrarPagoReembolsoSistema(UUID idReembolso, RegistrarPagoReembolsoSistemaDTO dto, String emailAdmin){
        LocalDateTime ahora = LocalDateTime.now();

        AdministradorSistemas administrador = administradorSistemasRepository.findByEmailActivo(emailAdmin)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.NOT_FOUND));

        Reembolso reembolso = reembolsoRepository.findByIdAndEstadoActualIn(idReembolso, ESTADOS_REEMBOLSO_ADMIN)
                .orElseThrow(ReembolsoNotFoundException::new);
        EstadoReembolsoNombre estadoReembolso = reembolso.getEstadoActual().getEstadoReembolso().getNombre();
        if (estadoReembolso != EstadoReembolsoNombre.IMPAGO)
            throw new ReembolsoNoImpagoException(estadoReembolso);

        // Pago hecho por el administrador, ya aprobado al momento de la transacción
        Pago pago = new Pago();
        pago.setFechaHoraPago(dto.fechaHoraPago());
        pago.setMontoTotal(reembolso.getMontoReembolso());
        pago.setIdTransaccionExterna(dto.idPagoExterno());
        pago.setMetodoPago(MetodoPago.MANUAL);
        pago.cambiarEstado(getEstadoPago(EstadoPagoNombre.APROBADO), dto.fechaHoraPago());

        ReembolsoSistemaPago reembolsoSistemaPago = new ReembolsoSistemaPago();
        reembolsoSistemaPago.setMetodoPagoReal(dto.metodoPago());
        reembolsoSistemaPago.setFechaHoraCarga(ahora);
        reembolsoSistemaPago.setReembolso(reembolso);
        reembolsoSistemaPago.setPago(pago);
        reembolsoSistemaPago.setCargadoPorAdmin(administrador);

        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.REEMBOLSADO_SISTEMA), ahora);
        reembolso.setFechaHoraReembolso(dto.fechaHoraPago());

        Reserva reserva = reembolso.getReserva();
        completarReservaReembolsada(reserva, ahora);

        // Se crea la deuda al productor
        deudaEstablecimientoService.crearNuevaDeuda(reembolso, reserva.getActividad().getEstablecimiento());

        reembolsoSistemaPagoRepository.save(reembolsoSistemaPago);
        reembolsoRepository.save(reembolso);
        reservaRepository.save(reserva);
        reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.REEMBOLSO_REALIZADO, reembolso.getMontoReembolso().toPlainString());
        reservaNotificador.notificarProductoresReembolso(reembolso);

        return reembolsoMapper.reembolsoToDetalleReembolsoAdminDTO(reembolso, reembolsoSistemaPago);
    }

    /**
     * Workaround al todavía no tener notificaciones webhook de las pasarelas.
     * <p>
     * Tarea programada que busca todos los reembolsos "En proceso" y consulta, mediante la estrategia del
     * método de pago de cada uno, si la pasarela ya lo resolvió.
     * <p>
     * Si fue aprobado, el reembolso pasa a "Reembolsado por productor", el pago a "Reembolsado" y la reserva
     * a "Cancelada con reembolso". Si fue rechazado, el reembolso pasa a "Pedido" para que lo haga el productor
     * y la reserva queda como está. En ambos casos se avisa al visitante y a los productores (ver
     * {@link #pasarAPedidoYNotificar}). Si sigue en proceso, se vuelve a consultar en la próxima ejecución.
     * <p>
     * Los reembolsos sin ID externo todavía no se pidieron (por ejemplo, los de un día cancelado) o se pidieron
     * sin poder guardar la respuesta. Pasado {@link #ESPERA_REEMBOLSO_SIN_CONFIRMAR} (para no pisar un pedido todavía
     * en curso), se busca el reembolso en la pasarela: si existe se sigue con él, si no se pide con la clave de
     * idempotencia del intento (ver {@link #claveIdempotencia}), así un pedido que sí llegó no se hace dos veces.
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
                    // Se mide desde el último intento: si lo reintentó el productor, fechaHoraPedido es la del primero
                    if (r.getEstadoActual().getFechaHoraInicio().isAfter(ahora.minus(ESPERA_REEMBOLSO_SIN_CONFIRMAR))) continue;

                    Optional<String> idExterno = getEstrategiaPago(pago).buscarReembolso(pago);
                    if (idExterno.isEmpty()){
                        EstadoReembolsoNombre estado = pedirReembolso(r, pago, claveIdempotencia(r, esReintento(r)));
                        if (estado != EstadoReembolsoNombre.EN_PROCESO) reembolsosResueltos++;
                        continue;
                    }
                    // Si sigue en proceso no se guarda el ID, se vuelve a buscar en la próxima ejecución
                    r.setIdReembolsoExterno(idExterno.get());
                }

                ResultadoConsultaReembolso resultado = getEstrategiaPago(pago).consultarReembolso(pago, r.getIdReembolsoExterno());

                switch (resultado){
                    case APROBADO -> {
                        completarReembolso(r, ahora);
                        self.guardarReembolsoYNotificar(r, TipoNotificacionNombre.REEMBOLSO_REALIZADO);
                    }
                    case RECHAZADO -> pasarAPedidoYNotificar(r, ahora);
                    case EN_PROCESO -> { continue; }
                }

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

    /**
     * Tarea programada que pasa a "Impago" los reembolsos "Pedido" que el productor no pagó dentro de
     * {@code diasImpagoReserva} días desde que se pidió el reembolso. A partir de ahí lo tiene que pagar un
     * administrador de sistemas (ver {@link #registrarPagoReembolsoSistema}). La reserva no cambia de estado.
     * Se avisa al visitante, a los productores y a los administradores que pueden ver los reembolsos impagos.
     * <p>
     * Si el parámetro no está cargado no se marca ningún reembolso. Los errores al procesar un reembolso particular
     * no interrumpen el resto y quedan registrados en el log.
     */
    @Transactional(readOnly = true)
    public void marcarReembolsosImpagos(){
        LocalDateTime ahora = LocalDateTime.now();
        Integer diasImpagoReserva = parametrosService.getInstance().getDiasImpagoReserva();
        if (diasImpagoReserva == null){
            log.warn("No está cargado el parámetro diasImpagoReserva, no se marcan reembolsos impagos");
            return;
        }

        List<UUID> idsVencidos = reembolsoRepository.findIdsPedidosVencidos(ahora.minusDays(diasImpagoReserva));
        if (idsVencidos.isEmpty()) return;

        List<String> idFallidos = new ArrayList<>(); // Array de las id de reembolso que fallaron al procesarse
        for (UUID reembolsoId : idsVencidos){
            try {
                self.marcarReembolsoImpago(reembolsoId, ahora);
            } catch (Exception e) {
                log.warn("Error de backend marcando como impago el reembolso {}", reembolsoId, e);
                idFallidos.add(reembolsoId.toString());
            }
        }

        log.info("Se marcaron {}/{} reembolsos pedidos como impagos. Los reembolsos con cambio fallido fueron {}, con ids: {}",
                idsVencidos.size() - idFallidos.size(), idsVencidos.size(), idFallidos.size(), idFallidos);
    }

    /**
     * Pasa un único reembolso a "Impago" en su propia transacción, para que un fallo al guardar uno no revierta
     * los ya confirmados.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void marcarReembolsoImpago(UUID reembolsoId, LocalDateTime ahora){
        // Bloqueado, como en iniciarReintentoReembolso: si el productor lo reintenta a la vez, gana el primero
        Reembolso reembolso = reembolsoRepository.findByIdForUpdate(reembolsoId).orElseThrow();

        // Relectura defensiva: entre la consulta y esta transacción el reembolso pudo dejar de estar "Pedido"
        if (reembolso.getEstadoActual().getEstadoReembolso().getNombre() != EstadoReembolsoNombre.PEDIDO)
            return;

        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.IMPAGO), ahora);
        reembolsoRepository.save(reembolso);

        Reserva reserva = reembolso.getReserva();
        String monto = reembolso.getMontoReembolso().toPlainString();
        reservaNotificador.notificarVisitante(reserva, TipoNotificacionNombre.REEMBOLSO_A_CARGO_AGROTOURS, monto);
        reservaNotificador.notificarProductoresReembolso(reembolso);
        reservaNotificador.notificarAdministradoresReembolsoImpago(reembolso);
    }

    /**
     * Reintento de un reembolso "Pedido" a pedido del productor.
     * <p>
     * Primero se vuelve a poner "En proceso" sin ID externo y después se busca en la pasarela si el reembolso ya
     * existe (por ejemplo, si el productor lo hizo desde la pasarela): si existe se concilia con él. Si no, se pide
     * de nuevo con los mismos pasos que {@link ReservaService#handleCancelarReserva}, con una clave de idempotencia propia del
     * intento (con la del primer pedido la pasarela devolvería el mismo rechazo). Si no se sabe qué pasó en la
     * pasarela queda "En proceso" y lo resuelve {@link #confirmarReembolsos()}.
     *
     * @throws ReembolsoNotFoundException si el reembolso no existe o no es del establecimiento
     * @throws ReembolsoNoPendienteException si el reembolso no está "Pedido"
     */
    public ResultadoReembolsoProductorDTO reembolsarPorProductor(UUID establecimientoId, UUID reembolsoId){
        // Paso 1: volver a poner el reembolso "En proceso", antes de mover dinero
        Reembolso reembolso = self.iniciarReintentoReembolso(establecimientoId, reembolsoId);
        Pago pago = reembolso.getReserva().getPago();

        Optional<String> idExistente;
        try {
            idExistente = getEstrategiaPago(pago).buscarReembolso(pago);
        } catch (PasarelaPagoException e) {
            log.warn("No se pudo buscar el reembolso {} en la pasarela, lo verificará la tarea programada", reembolso.getId(), e);
            return new ResultadoReembolsoProductorDTO(ResultadoReembolsoProductor.EN_PROCESO);
        }

        if (idExistente.isPresent())
            return conciliarReembolsoExistente(reembolso, pago, idExistente.get());

        // Pasos 2 y 3: pedir el reembolso a la pasarela y guardar su respuesta
        EstadoReembolsoNombre estado = pedirReembolso(reembolso, pago, claveIdempotencia(reembolso, true));

        if (estado == EstadoReembolsoNombre.PEDIDO)
            return new ResultadoReembolsoProductorDTO(ResultadoReembolsoProductor.RECHAZADO);
        return new ResultadoReembolsoProductorDTO(ResultadoReembolsoProductor.EN_PROCESO);
    }

    /**
     * Concilia el reintento del productor con un reembolso que ya existe en la pasarela: si está aprobado se da por
     * realizado; si no, se guarda su ID externo y lo termina de confirmar {@link #confirmarReembolsos()}.
     */
    private ResultadoReembolsoProductorDTO conciliarReembolsoExistente(Reembolso reembolso, Pago pago, String idExterno){
        ResultadoConsultaReembolso consulta;
        try {
            consulta = getEstrategiaPago(pago).consultarReembolso(pago, idExterno);
        } catch (PasarelaPagoException e) {
            log.warn("No se pudo consultar el reembolso {} en la pasarela, lo verificará la tarea programada", reembolso.getId(), e);
            consulta = ResultadoConsultaReembolso.EN_PROCESO;
        }

        if (consulta == ResultadoConsultaReembolso.APROBADO) {
            reembolso.setIdReembolsoExterno(idExterno);
            completarReembolso(reembolso, LocalDateTime.now());
            self.guardarReembolsoYNotificar(reembolso, TipoNotificacionNombre.REEMBOLSO_REALIZADO);
            return new ResultadoReembolsoProductorDTO(ResultadoReembolsoProductor.REALIZADO);
        }

        self.registrarPedidoReembolso(reembolso.getId(), new ResultadoReembolsoDTO(true, idExterno));
        return new ResultadoReembolsoProductorDTO(ResultadoReembolsoProductor.EN_PROCESO);
    }

    /**
     * Crea y guarda un reembolso "En proceso", sin ID externo, por el monto indicado. Va en la transacción del que lo
     * llama, junto con el cambio de estado de la reserva.
     */
    public Reembolso crearReembolsoEnProceso(Reserva reserva, BigDecimal monto, LocalDateTime ahora) {
        Reembolso reembolso = new Reembolso();
        reembolso.setMontoReembolso(monto);
        reembolso.setFechaHoraPedido(ahora);
        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.EN_PROCESO), ahora);
        reembolso.setReserva(reserva);

        reembolsoRepository.save(reembolso);
        return reembolso;
    }

    /**
     * Guarda un reembolso junto con su reserva (y el pago de ésta) y avisa al visitante y a los productores, en su
     * propia transacción para que un fallo al guardar un reembolso no revierta los ya confirmados.
     *
     * @param reembolso reembolso a guardar
     * @param aviso tipo de notificación al visitante con el nuevo estado del reembolso, o null para no avisarle
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void guardarReembolsoYNotificar(Reembolso reembolso, TipoNotificacionNombre aviso){
        reservaRepository.save(reembolso.getReserva());
        reembolsoRepository.save(reembolso);
        if (aviso != null)
            reservaNotificador.notificarVisitante(reembolso.getReserva(), aviso, reembolso.getMontoReembolso().toPlainString());
        reservaNotificador.notificarProductoresReembolso(reembolso);
    }

    /**
     * Pasa a "Pedido" un reembolso que la pasarela no hizo, lo guarda y avisa a los productores, que lo tienen que
     * realizar. Al visitante solo se le avisa la primera vez: si ya estuvo "Pedido", es un reintento fallido del
     * productor y el visitante ya sabe que su reembolso se gestiona de forma manual.
     */
    private void pasarAPedidoYNotificar(Reembolso reembolso, LocalDateTime ahora){
        boolean yaEstuvoPedido = reembolso.getEstados().stream()
                .anyMatch(e -> e.getEstadoReembolso().getNombre() == EstadoReembolsoNombre.PEDIDO);

        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.PEDIDO), ahora);
        self.guardarReembolsoYNotificar(reembolso, yaEstuvoPedido ? null : TipoNotificacionNombre.REEMBOLSO_NO_AUTOMATICO);
    }

    /**
     * Paso 1 de {@link #reembolsarPorProductor}: valida que el reembolso sea del establecimiento y esté "Pedido",
     * le quita el ID externo del intento anterior (si no, la tarea programada consultaría el reembolso rechazado)
     * y lo vuelve a poner "En proceso". La fecha de pedido no cambia: sigue siendo la del primer pedido.
     * <p>
     * El reembolso se bloquea para que un doble click no lance dos intentos.
     *
     * @return el reembolso "En proceso", con su reserva y pago
     */
    @Transactional
    protected Reembolso iniciarReintentoReembolso(UUID establecimientoId, UUID reembolsoId){
        Reembolso reembolso = reembolsoRepository.findByIdAndEstablecimiento(reembolsoId, establecimientoId)
                .orElseThrow(ReembolsoNotFoundException::new);

        EstadoReembolsoNombre estado = reembolso.getEstadoActual().getEstadoReembolso().getNombre();
        if (estado != EstadoReembolsoNombre.PEDIDO)
            throw new ReembolsoNoPendienteException(estado);

        reembolso.setIdReembolsoExterno(null);
        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.EN_PROCESO), LocalDateTime.now());
        return reembolsoRepository.save(reembolso);
    }

    /**
     * Pide el reembolso a la pasarela y guarda su respuesta (pasos 2 y 3 de {@link ReservaService#handleCancelarReserva} y
     * {@link ReservaService#reembolsarPagoNoConciliable}). Se llama fuera de toda transacción, con el reembolso "En proceso"
     * ya guardado. Nunca se reintenta el pedido: si no se sabe si la pasarela lo hizo (error de comunicación o falla al
     * guardar la respuesta), el reembolso queda "En proceso" y {@link #confirmarReembolsos()} lo verifica.
     *
     * @return el estado en que quedó el reembolso: "En proceso" (aceptado o sin saber), "Pedido" (rechazado) o
     * "Reembolsado por productor" (pago manual, se completa al pedirlo)
     */
    public EstadoReembolsoNombre pedirReembolso(Reembolso reembolso){
        return pedirReembolso(reembolso, reembolso.getReserva().getPago(), claveIdempotencia(reembolso, false));
    }

    /**
     * Clave de idempotencia del intento actual del reembolso, para que pedirlo de nuevo (por ejemplo desde
     * {@link #confirmarReembolsos()}) no lo haga dos veces. El primer pedido usa la del pago; cada reintento del
     * productor usa una propia, porque con la del primer pedido la pasarela devolvería el mismo rechazo.
     */
    private String claveIdempotencia(Reembolso reembolso, boolean esReintento){
        if (!esReintento)
            return "reembolso-pago-" + reembolso.getReserva().getPago().getId();

        long inicioIntento = reembolso.getEstadoActual().getFechaHoraInicio().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return "reembolso-" + reembolso.getId() + "-" + inicioIntento;
    }

    // Un reembolso que ya estuvo "Pedido" y vuelve a estar "En proceso" es un reintento del productor
    private static boolean esReintento(Reembolso reembolso){
        return reembolso.getEstados().stream()
                .anyMatch(e -> e.getEstadoReembolso().getNombre() == EstadoReembolsoNombre.PEDIDO);
    }

    /**
     * Como {@link #pedirReembolso(Reembolso)}, con la clave de idempotencia del intento.
     */
    private EstadoReembolsoNombre pedirReembolso(Reembolso reembolso, Pago pago, String claveIdempotencia){
        ResultadoReembolsoDTO resultado;
        try {
            resultado = getEstrategiaPago(pago).reembolsar(pago, claveIdempotencia);
        } catch (PasarelaPagoException e) {
            log.warn("No se sabe si se realizó el reembolso {}, lo verificará la tarea programada", reembolso.getId(), e);
            return EstadoReembolsoNombre.EN_PROCESO;
        }

        try {
            return self.registrarPedidoReembolso(reembolso.getId(), resultado);
        } catch (Exception e) {
            log.error("No se pudo guardar la respuesta de la pasarela del reembolso {}, lo verificará la tarea programada", reembolso.getId(), e);
            return EstadoReembolsoNombre.EN_PROCESO;
        }
    }

    /**
     * Paso 3 de {@link #pedirReembolso}: guarda la respuesta de la pasarela al pedido de reembolso.
     * <ul>
     *     <li>Pago manual: no hay pasarela, el reembolso se da por realizado y se avisa.</li>
     *     <li>Aceptado: se guarda su ID externo y sigue "En proceso" hasta que se confirme.</li>
     *     <li>Rechazado: pasa a "Pedido" para que lo haga el productor. Se avisa al visitante y a los productores solo la
     *     primera vez: si ya estuvo "Pedido" es un reintento del productor, que ve el rechazo en la respuesta.</li>
     * </ul>
     * Va en su propia transacción porque {@link ReservaService#pagarReservas()} lo llama dentro de una de solo lectura, en la
     * que no se guardaría nada.
     *
     * @return el estado en que quedó el reembolso
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected EstadoReembolsoNombre registrarPedidoReembolso(UUID reembolsoId, ResultadoReembolsoDTO resultado){
        Reembolso reembolso = reembolsoRepository.findById(reembolsoId).orElseThrow();
        LocalDateTime ahora = LocalDateTime.now();

        if (reembolso.getReserva().getPago().getMetodoPago() == MetodoPago.MANUAL) {
            completarReembolso(reembolso, ahora);
            guardarReembolsoYNotificar(reembolso, TipoNotificacionNombre.REEMBOLSO_REALIZADO);
        } else if (resultado.aceptado()) {
            reembolso.setIdReembolsoExterno(resultado.idReembolsoExterno());
            reembolsoRepository.save(reembolso);
        } else {
            boolean primerRechazo = !esReintento(reembolso);

            reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.PEDIDO), ahora);
            reembolsoRepository.save(reembolso);
            if (primerRechazo) {
                reservaNotificador.notificarVisitante(reembolso.getReserva(), TipoNotificacionNombre.REEMBOLSO_NO_AUTOMATICO,
                        reembolso.getMontoReembolso().toPlainString());
                reservaNotificador.notificarProductoresReembolso(reembolso);
            }
        }

        return reembolso.getEstadoActual().getEstadoReembolso().getNombre();
    }

    /**
     * Da por realizado un reembolso: el reembolso pasa a "Reembolsado por productor" con su fecha de reembolso,
     * el pago a "Reembolsado" y, si es por una cancelación, la reserva a "Cancelada con reembolso". La reserva
     * de un pago no conciliado ya está en su estado final ("Reembolsada por pago no válido") y no cambia. No guarda.
     *
     * @param reembolso reembolso realizado, con su reserva y pago
     * @param ahora fecha y hora en la que se realizó el reembolso
     */
    public void completarReembolso(Reembolso reembolso, LocalDateTime ahora){
        reembolso.cambiarEstado(getEstadoReembolso(EstadoReembolsoNombre.REEMBOLSADO_PRODUCTOR), ahora);
        reembolso.setFechaHoraReembolso(ahora);
        completarReservaReembolsada(reembolso.getReserva(), ahora);
    }

    /**
     * Refleja en la reserva un reembolso ya realizado (por el productor o por el sistema): su pago pasa a
     * "Reembolsado" y, si es por una cancelación, la reserva a "Cancelada con reembolso". La reserva de un pago
     * no conciliado ya está en su estado final ("Reembolsada por pago no válido") y no cambia. No guarda.
     *
     * @param reserva reserva del reembolso, con su pago
     * @param ahora fecha y hora del cambio
     */
    private void completarReservaReembolsada(Reserva reserva, LocalDateTime ahora){
        reserva.getPago().cambiarEstado(getEstadoPago(EstadoPagoNombre.REEMBOLSADO), ahora);
        if (reserva.getEstadoActual().getEstadoReserva().getNombre() != REEMBOLSO_NO_CONCILIACION)
            reserva.cambiarEstado(getEstadoReserva(CANCELADA_CON_REEMBOLSO), ahora);
    }

    // El pago es obligatorio para pedir, buscar o consultar un reembolso
    private EstrategiaPago getEstrategiaPago(Pago pago){
        Objects.requireNonNull(pago, "Se necesita el pago para obtener su estrategia");
        return estrategiaPagoFactory.get(pago.getMetodoPago());
    }

    private EstadoReserva getEstadoReserva(EstadoReservaNombre estadoReservaNombre){
        return reservaRepository.findEstadoReservaByEstadoReservaNombre(estadoReservaNombre)
                .orElseThrow(() -> new EstadoReservaNotFoundException(estadoReservaNombre));
    }

    private EstadoPago getEstadoPago(EstadoPagoNombre estadoPagoNombre){
        return estadoPagoRepository.findByNombre(estadoPagoNombre)
                .orElseThrow(() -> new EstadoPagoNotFoundException(estadoPagoNombre));
    }

    private EstadoReembolso getEstadoReembolso(EstadoReembolsoNombre estadoReembolsoNombre){
        return estadoReembolsoRepository.findByNombre(estadoReembolsoNombre)
                .orElseThrow(() -> new EstadoReembolsoNotFoundException(estadoReembolsoNombre));
    }
}
