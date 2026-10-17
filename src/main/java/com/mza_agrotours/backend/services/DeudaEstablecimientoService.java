package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.deudas.DeudaDetalleDTO;
import com.mza_agrotours.backend.dtos.deudas.DeudaEstadoDTO;
import com.mza_agrotours.backend.dtos.deudas.ListarDeudaDTO;
import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimiento;
import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimientoEstado;
import com.mza_agrotours.backend.entities.deudas.EstadoDeudaEstablecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.enums.EstadoDeudaEstablecimientoNombre;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.repositories.DeudaRepository;
import com.mza_agrotours.backend.repositories.EstadoDeudaEstablecimientoRepository;
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

@Service
public class DeudaEstablecimientoService {
    private final DeudaRepository deudaRepository;
    private final EstadoDeudaEstablecimientoRepository estadoDeudaEstablecimientoRepository;

    public DeudaEstablecimientoService(DeudaRepository deudaRepository, EstadoDeudaEstablecimientoRepository estadoDeudaEstablecimientoRepository) {
        this.deudaRepository = deudaRepository;
        this.estadoDeudaEstablecimientoRepository = estadoDeudaEstablecimientoRepository;
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
     * Detalle de una deuda con su historial de estados, del más antiguo al más reciente.
     */
    @Transactional(readOnly = true)
    public DeudaDetalleDTO detalleDeuda(UUID idDeuda){
        DeudaEstablecimiento deuda = deudaRepository.findDetalleById(idDeuda)
                .orElseThrow(() -> new EntityNotFoundException("La deuda no pudo ser encontrada"));

        List<DeudaEstadoDTO> estados = deuda.getEstados()
                .stream()
                .sorted(Comparator.comparing(DeudaEstablecimientoEstado::getFechaHoraInicio))
                .map(this::toDeudaEstadoDTO)
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

    private DeudaEstadoDTO toDeudaEstadoDTO(DeudaEstablecimientoEstado estado){
        return new DeudaEstadoDTO(
                estado.getEstadoDeudaEstablecimiento().getNombre().name(),
                estado.getFechaHoraInicio(),
                estado.getFechaHoraFin(),
                estado.getMontoDevueltoTransaccion(),
                estado.getMotivo()
        );
    }
}