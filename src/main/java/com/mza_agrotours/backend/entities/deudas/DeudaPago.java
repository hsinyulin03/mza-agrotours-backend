package com.mza_agrotours.backend.entities.deudas;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.pago.Pago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Pago con el que un establecimiento devolvió (parte de) una deuda, cargado por un administrador de sistemas.
 * Vincula el cambio de estado de la deuda que generó con el pago cargado, quién lo cargó y con qué medio se pagó realmente.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeudaPago extends BaseEntity {

    // Medio con el que se hizo la transacción. Texto libre: puede ser uno que el sistema no soporta como método de pago
    @Column(nullable = false)
    private String metodoPagoReal;

    @Column(nullable = false)
    private LocalDateTime fechaHoraCarga;

    // Único: cada cambio de estado de la deuda tiene a lo sumo un pago
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private DeudaEstablecimientoEstado deudaEstablecimientoEstado;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Pago pago;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AdministradorSistemas cargadoPorAdmin;
}
