package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.deudas.ListarDeudaDTO;
import com.mza_agrotours.backend.entities.deudas.DeudaEstablecimiento;
import com.mza_agrotours.backend.entities.deudas.EstadoDeudaEstablecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.enums.EstadoDeudaEstablecimientoNombre;
import com.mza_agrotours.backend.repositories.DeudaRepository;
import com.mza_agrotours.backend.repositories.EstadoDeudaEstablecimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void crearNuevaDeuda(Reembolso reembolso, Establecimiento establecimiento){
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
        deudaRepository.save(deuda);
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
}