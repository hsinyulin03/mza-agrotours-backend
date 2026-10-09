package com.mza_agrotours.backend.entities.pago;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pago extends BaseEntity {

    // ID de la transacción en el medio de pago. En MP es el ID del payment aprobado: es null hasta que se concilia
    private String idTransaccionExterna;

    // ID del checkout con el que se cobra. En MP es el ID de la preference de Checkout Pro
    private String idCheckoutExterno;

    @Column(nullable = false)
    private LocalDateTime fechaHoraPago;

    @Column(nullable = false)
    private BigDecimal montoTotal;

    // NOTE Esto no estaba en el DC ni la US, pero lo veo útil para usar estrategia y hacer pagos manuales y por mp
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    // Cuenta de MP del vendedor con cuyo token se creó la preference. Toda llamada posterior a MP sobre este
    // pago (consulta, expiración, reembolso) debe usar esta misma cuenta, aunque el establecimiento haya
    // vinculado otra después. Null en pagos manuales y en preferences anteriores al split (cuenta de Agrotours).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_mercado_pago_id")
    private CuentaMercadoPago cuentaMercadoPago;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private PagoEstado estadoActual;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "pago_id")
    private List<PagoEstado> estados = new ArrayList<>();

    /**
     * Realiza un cambio de estado de un pago. <p></p>
     * Incluye la creación de nuevas instancias, relaciones y cambios en los atributos de las clases involucradas.
     * @param estado Estado al que se quiere cambiar el pago
     * @param tiempoCambio Fecha y hora a la que se realizó el cambio
     */
    public void cambiarEstado(EstadoPago estado, LocalDateTime tiempoCambio){
        // Al último estado le damos FechaHoraFin, si es que había uno (primer estado del pago)
        if (this.estadoActual != null)
            this.estadoActual.setFechaHoraFin(tiempoCambio);

        // Creamos la nueva ReservaEstado
        PagoEstado nuevoRE = new PagoEstado(tiempoCambio, null, estado);

        // Agregamos la nueva ReservaEstado a las relaciones
        this.estadoActual = nuevoRE;
        this.estados.add(nuevoRE);
    }
}
