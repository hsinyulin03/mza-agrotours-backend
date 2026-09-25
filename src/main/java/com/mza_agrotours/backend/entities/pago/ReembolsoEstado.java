package com.mza_agrotours.backend.entities.pago;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReembolsoEstado extends BaseEntity {
    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    // Relaciones
    @ManyToOne(optional = false)
    @JoinColumn(name = "estado_reembolso_id", nullable = false)
    private EstadoReembolso estadoReembolso;
}
