package com.mza_agrotours.backend.dtos.pago;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ReembolsoGetCatalogoDTO {
    // Datos reembolso
    private UUID reembolsoId;
    private LocalDateTime fechaHoraPedido;
    private BigDecimal montoReembolso;

    // Datos visitante
    private String nombreVisitante;
    private String emailVisitante;

    // Datos reserva
    private UUID reservaId;
    private Integer cuposReservados;

    // Datos actividad
    private String nombreActividad;
    private LocalDateTime diaActividad;

    // Proyección de ReembolsoRepository
    public ReembolsoGetCatalogoDTO(UUID reembolsoId, LocalDateTime fechaHoraPedido, BigDecimal montoReembolso,
                                   String nombreVisitante, String emailVisitante, UUID reservaId, Integer cuposReservados,
                                   String nombreActividad, LocalDateTime diaActividad) {
        this.reembolsoId = reembolsoId;
        this.fechaHoraPedido = fechaHoraPedido;
        this.montoReembolso = montoReembolso;
        this.nombreVisitante = nombreVisitante;
        this.emailVisitante = emailVisitante;
        this.reservaId = reservaId;
        this.cuposReservados = cuposReservados;
        this.nombreActividad = nombreActividad;
        this.diaActividad = diaActividad;
    }
}
