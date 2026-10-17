package com.mza_agrotours.backend.entities.deudas;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DeudaEstablecimientoEstado extends BaseEntity {
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    private BigDecimal montoDevueltoTransaccion; // Cuánto se devolvió en esta transacción
    private String motivo; // Motivo opcional para agregar info a la transacción

    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_deuda_establecimiento_id", nullable = false)
    private EstadoDeudaEstablecimiento estadoDeudaEstablecimiento;
}
