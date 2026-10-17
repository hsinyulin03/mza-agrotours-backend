package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.config.RutasNotificacionesFront;
import com.mza_agrotours.backend.dtos.deudas.CambiarEstadoDeudaDTO;
import com.mza_agrotours.backend.dtos.deudas.DeudaDetalleDTO;
import com.mza_agrotours.backend.dtos.deudas.DeudaEstadoDTO;
import com.mza_agrotours.backend.dtos.deudas.DeudaPagoDTO;
import com.mza_agrotours.backend.dtos.deudas.ListarDeudaDTO;
import com.mza_agrotours.backend.dtos.deudas.PagoDeudaDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimiento;
import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimientoEstado;
import com.mza_agrotours.backend.entities.deudas.DeudaPago;
import com.mza_agrotours.backend.entities.deudas.EstadoDeudaEstablecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.enums.EstadoDeudaEstablecimientoNombre;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.TipoCambioEstadoDeuda;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import com.mza_agrotours.backend.exceptions.AdministradorSistemasError;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.DeudaError;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.pago.EstadoPagoNotFoundException;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.DeudaPagoRepository;
import com.mza_agrotours.backend.repositories.DeudaRepository;
import com.mza_agrotours.backend.repositories.EstadoDeudaEstablecimientoRepository;
import com.mza_agrotours.backend.repositories.pago.EstadoPagoRepository;
import com.mza_agrotours.backend.services.notificaciones.NotificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DeudaEstablecimientoService {
    // Estados en los que todavía se debe algo; DEVOLUCION_TOTAL y ACUERDO_MEDIO son finales
    private static final List<EstadoDeudaEstablecimientoNombre> ESTADOS_DEUDA_ABIERTA =
            List.of(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER, EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL);

    private final DeudaRepository deudaRepository;
    private final DeudaPagoRepository deudaPagoRepository;
    private final EstadoDeudaEstablecimientoRepository estadoDeudaEstablecimientoRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final NotificacionService notificacionService;

    public DeudaEstablecimientoService(DeudaRepository deudaRepository, DeudaPagoRepository deudaPagoRepository, EstadoDeudaEstablecimientoRepository estadoDeudaEstablecimientoRepository, EstadoPagoRepository estadoPagoRepository, AdministradorSistemasRepository administradorSistemasRepository, NotificacionService notificacionService) {
        this.deudaRepository = deudaRepository;
        this.deudaPagoRepository = deudaPagoRepository;
        this.estadoDeudaEstablecimientoRepository = estadoDeudaEstablecimientoRepository;
        this.estadoPagoRepository = estadoPagoRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.notificacionService = notificacionService;
    }

    /**
     * Lista las deudas de todos los establecimientos con el sistema, de la más antigua a la más reciente.
     */
    @Transactional(readOnly = true)
    public List<ListarDeudaDTO> listarDeudas(){
        return deudaRepository.findAllConEstablecimientoYEstado()
                .stream()
                .map(this::toListarDeudaDTO)
                .toList();
    }

    /**
     * Detalle de una deuda con su historial de estados, del más antiguo al más reciente. Cada estado lleva el pago
     * con el que se devolvió, si tuvo.
     */
    @Transactional(readOnly = true)
    public DeudaDetalleDTO detalleDeuda(UUID idDeuda){
        DeudaEstablecimiento deuda = deudaRepository.findDetalleById(idDeuda)
                .orElseThrow(() -> new EntityNotFoundException("La deuda no pudo ser encontrada"));

        // Pagos de todos los estados en una sola consulta, por id de estado
        Map<UUID, DeudaPago> pagosPorEstado = deudaPagoRepository.findByEstadoIdIn(
                        deuda.getEstados().stream().map(DeudaEstablecimientoEstado::getId).toList())
                .stream()
                .collect(Collectors.toMap(dp -> dp.getDeudaEstablecimientoEstado().getId(), Function.identity()));

        List<DeudaEstadoDTO> estados = deuda.getEstados()
                .stream()
                .sorted(Comparator.comparing(DeudaEstablecimientoEstado::getFechaHoraInicio))
                .map(e -> toDeudaEstadoDTO(e, pagosPorEstado.get(e.getId())))
                .toList();

        return new DeudaDetalleDTO(
                deuda.getId().toString(),
                deuda.getFechaCreacionDeuda(),
                deuda.getEstablecimientoDeudor().getNombre(),
                deuda.getMontoDeuda(),
                deuda.getMontoTotalDevuelto(),
                estados
        );
    }

    /**
     * Id de la deuda que generó el reembolso, si el sistema ya lo pagó.
     */
    public Optional<UUID> getIdDeudaDeReembolso(UUID idReembolso){
        return deudaRepository.findIdByReembolsoId(idReembolso);
    }

    /**
     * Ids de las deudas generadas por cada reembolso, en una sola consulta. Los reembolsos sin deuda no aparecen.
     */
    public Map<UUID, UUID> getIdsDeudaPorReembolso(Collection<UUID> idsReembolso){
        if (idsReembolso.isEmpty())
            return Map.of();

        Map<UUID, UUID> idsDeuda = new HashMap<>();
        for (Object[] fila : deudaRepository.findIdsByReembolsoIdIn(idsReembolso))
            idsDeuda.put((UUID) fila[0], (UUID) fila[1]);
        return idsDeuda;
    }

    /**
     * Cambia el estado de una deuda todavía abierta (sin devolver o con devolución parcial), a pedido de un administrador.
     * <ul>
     *     <li>DEVOLUCION: el pago es obligatorio y no puede superar lo que resta devolver. Si con él se completa, la
     *     deuda pasa a "Devolución total"; si no, a "Devolución parcial".</li>
     *     <li>ACUERDO_MEDIO: la deuda se cierra por un monto acordado y se perdona el resto. El pago es opcional y
     *     puede llegar hasta lo que resta devolver.</li>
     * </ul>
     * El pago se guarda como un pago manual aprobado, vinculado al nuevo estado de la deuda junto con el
     * administrador que lo cargó y el medio de pago real. Avisa a los productores del establecimiento deudor.
     *
     * @param idDeuda deuda a cambiar
     * @param dto tipo de cambio, motivo y pago
     * @param emailAdmin email del administrador que lo carga
     * @return el detalle de la deuda ya cambiada
     * @throws AppException si el administrador no existe o no está activo, si la deuda ya está finalizada, si falta
     * el pago de una devolución, si el monto supera lo que resta devolver o si la deuda ya tiene un pago con ese ID externo
     * @throws EntityNotFoundException si la deuda no existe
     */
    @Transactional
    public DeudaDetalleDTO cambiarEstadoDeuda(UUID idDeuda, CambiarEstadoDeudaDTO dto, String emailAdmin){
        LocalDateTime ahora = LocalDateTime.now();

        AdministradorSistemas administrador = administradorSistemasRepository.findByEmailActivo(emailAdmin)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.NOT_FOUND));

        DeudaEstablecimiento deuda = deudaRepository.findByIdForUpdate(idDeuda)
                .orElseThrow(() -> new EntityNotFoundException("La deuda no pudo ser encontrada"));
        if (!ESTADOS_DEUDA_ABIERTA.contains(deuda.getEstadoActual().getEstadoDeudaEstablecimiento().getNombre()))
            throw new AppException(DeudaError.DEUDA_FINALIZADA);

        PagoDeudaDTO pagoDTO = dto.pago();
        if (dto.tipo() == TipoCambioEstadoDeuda.DEVOLUCION && pagoDTO == null)
            throw new AppException(DeudaError.PAGO_OBLIGATORIO);

        BigDecimal restante = deuda.getMontoDeuda().subtract(deuda.getMontoTotalDevuelto());
        BigDecimal montoDevuelto = pagoDTO != null ? pagoDTO.monto() : BigDecimal.ZERO;
        if (montoDevuelto.compareTo(restante) > 0)
            throw new AppException(DeudaError.MONTO_INVALIDO);

        // La misma transacción cargada dos veces sumaría lo devuelto de más
        if (pagoDTO != null && deudaPagoRepository.existsByDeudaIdAndIdPagoExterno(idDeuda, pagoDTO.idPagoExterno()))
            throw new AppException(DeudaError.PAGO_DUPLICADO);

        EstadoDeudaEstablecimientoNombre nuevoEstado;
        if (dto.tipo() == TipoCambioEstadoDeuda.ACUERDO_MEDIO)
            nuevoEstado = EstadoDeudaEstablecimientoNombre.ACUERDO_MEDIO;
        else if (montoDevuelto.compareTo(restante) == 0)
            nuevoEstado = EstadoDeudaEstablecimientoNombre.DEVOLUCION_TOTAL;
        else
            nuevoEstado = EstadoDeudaEstablecimientoNombre.DEVOLUCION_PARCIAL;

        deuda.cambiarEstado(getEstadoDeuda(nuevoEstado), ahora, montoDevuelto, dto.motivo());

        // Inserta el estado nuevo antes que el pago que lo referencia. No se usa save: el merge copiaría el estado nuevo
        deudaRepository.flush();

        if (pagoDTO != null)
            deudaPagoRepository.save(crearDeudaPago(pagoDTO, deuda.getEstadoActual(), administrador, ahora));

        notificarProductores(deuda, nuevoEstado, montoDevuelto);

        return detalleDeuda(idDeuda);
    }

    public DeudaEstablecimiento crearNuevaDeuda(Reembolso reembolso, Establecimiento establecimiento){
        LocalDateTime ahora = LocalDateTime.now();

        // Crear la entidad
        DeudaEstablecimiento deuda = new DeudaEstablecimiento(
                ahora,
                reembolso.getMontoReembolso(),
                BigDecimal.ZERO,
                reembolso,
                new ArrayList<>(),
                null,
                establecimiento
        );

        // Ponerle el estado inicial
        EstadoDeudaEstablecimiento estado = estadoDeudaEstablecimientoRepository.findByNombre(EstadoDeudaEstablecimientoNombre.SIN_DEVOLVER)
                .orElseThrow();
        String motivo = "Reembolso pagado por sistema";
        deuda.cambiarEstado(estado, ahora, BigDecimal.ZERO, motivo);

        // Guardar y devolver
        return deudaRepository.save(deuda);
    }

    // AUX
    private ListarDeudaDTO toListarDeudaDTO(DeudaEstablecimiento deuda){
        return new ListarDeudaDTO(
                deuda.getId().toString(),
                deuda.getFechaCreacionDeuda(),
                deuda.getEstablecimientoDeudor().getNombre(),
                deuda.getMontoDeuda(),
                deuda.getMontoTotalDevuelto(),
                deuda.getEstadoActual().getEstadoDeudaEstablecimiento().getNombre().name()
        );
    }

    private DeudaEstadoDTO toDeudaEstadoDTO(DeudaEstablecimientoEstado estado, DeudaPago deudaPago){
        return new DeudaEstadoDTO(
                estado.getEstadoDeudaEstablecimiento().getNombre().name(),
                estado.getFechaHoraInicio(),
                estado.getFechaHoraFin(),
                estado.getMontoDevueltoTransaccion(),
                estado.getMotivo(),
                deudaPago != null ? toDeudaPagoDTO(deudaPago) : null
        );
    }

    private DeudaPagoDTO toDeudaPagoDTO(DeudaPago deudaPago){
        Pago pago = deudaPago.getPago();
        return new DeudaPagoDTO(
                pago.getId().toString(),
                pago.getFechaHoraPago(),
                pago.getIdTransaccionExterna(),
                pago.getMontoTotal(),
                deudaPago.getMetodoPagoReal(),
                deudaPago.getFechaHoraCarga(),
                deudaPago.getCargadoPorAdmin().getUsuario().getNombre()
        );
    }

    // Pago hecho por el establecimiento, ya aprobado al momento de la transacción
    private DeudaPago crearDeudaPago(PagoDeudaDTO dto, DeudaEstablecimientoEstado estado, AdministradorSistemas administrador, LocalDateTime ahora){
        Pago pago = new Pago();
        pago.setFechaHoraPago(dto.fechaHoraPago());
        pago.setMontoTotal(dto.monto());
        pago.setIdTransaccionExterna(dto.idPagoExterno());
        pago.setMetodoPago(MetodoPago.MANUAL);
        pago.cambiarEstado(estadoPagoRepository.findByNombre(EstadoPagoNombre.APROBADO)
                .orElseThrow(() -> new EstadoPagoNotFoundException(EstadoPagoNombre.APROBADO)), dto.fechaHoraPago());

        DeudaPago deudaPago = new DeudaPago();
        deudaPago.setMetodoPagoReal(dto.metodoPago());
        deudaPago.setFechaHoraCarga(ahora);
        deudaPago.setDeudaEstablecimientoEstado(estado);
        deudaPago.setPago(pago);
        deudaPago.setCargadoPorAdmin(administrador);
        return deudaPago;
    }

    // Avisa a los productores que gestionan los reembolsos, los mismos que recibieron el aviso del reembolso pagado por el sistema
    private void notificarProductores(DeudaEstablecimiento deuda, EstadoDeudaEstablecimientoNombre estado, BigDecimal montoDevuelto){
        String montoDeuda = deuda.getMontoDeuda().toPlainString();
        TipoNotificacionNombre tipo;
        Object[] datos;
        switch (estado) {
            case DEVOLUCION_PARCIAL -> {
                tipo = TipoNotificacionNombre.DEUDA_DEVOLUCION_PARCIAL_PRODUCTOR;
                datos = new Object[]{montoDevuelto.toPlainString(), montoDeuda,
                        deuda.getMontoDeuda().subtract(deuda.getMontoTotalDevuelto()).toPlainString()};
            }
            case DEVOLUCION_TOTAL -> {
                tipo = TipoNotificacionNombre.DEUDA_DEVOLUCION_TOTAL_PRODUCTOR;
                datos = new Object[]{montoDevuelto.toPlainString(), montoDeuda};
            }
            case ACUERDO_MEDIO -> {
                tipo = TipoNotificacionNombre.DEUDA_ACUERDO_MEDIO_PRODUCTOR;
                datos = new Object[]{montoDeuda, deuda.getMontoTotalDevuelto().toPlainString()};
            }
            default -> { return; }
        }

        Establecimiento establecimiento = deuda.getEstablecimientoDeudor();
        notificacionService.notificarProductores(
                establecimiento,
                PermisoCodigo.GESTIONAR_REEMBOLSOS_PENDIENTES,
                tipo,
                RutasNotificacionesFront.panelEstablecimiento(establecimiento.getId()),
                datos);
    }

    private EstadoDeudaEstablecimiento getEstadoDeuda(EstadoDeudaEstablecimientoNombre nombre){
        return estadoDeudaEstablecimientoRepository.findByNombre(nombre).orElseThrow();
    }
}