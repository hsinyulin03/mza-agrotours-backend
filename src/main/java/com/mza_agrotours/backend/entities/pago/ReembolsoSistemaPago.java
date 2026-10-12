package com.mza_agrotours.backend.entities.pago;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Pago que hizo un administrador de sistemas para cubrir un reembolso que el productor no pagó.
 * Vincula el reembolso con el pago cargado y guarda quién lo cargó y con qué medio se pagó realmente.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReembolsoSistemaPago extends BaseEntity {

    // Medio con el que se hizo la transacción. Texto libre: puede ser uno que el sistema no soporta como método de pago
    @Column(nullable = false)
    private String metodoPagoReal;

    @Column(nullable = false)
    private LocalDateTime fechaHoraCarga;

    // Único: un reembolso se paga por sistema una sola vez
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private Reembolso reembolso;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Pago pago;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AdministradorSistemas cargadoPorAdmin;
}
