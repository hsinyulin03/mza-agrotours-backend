package com.mza_agrotours.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pago extends BaseEntity {

    @Column(nullable = false)
    private String idPagoExterno;

    @Column(nullable = false)
    private LocalDateTime fechaHoraPago;

    @Column(nullable = false)
    private BigDecimal montoTotal;
}
