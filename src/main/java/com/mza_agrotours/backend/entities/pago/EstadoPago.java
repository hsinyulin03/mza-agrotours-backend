package com.mza_agrotours.backend.entities.pago;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EstadoPago extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPagoNombre nombre;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
}
