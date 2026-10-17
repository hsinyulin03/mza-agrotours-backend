package com.mza_agrotours.backend.entities.deudas;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DeudaEstablecimiento extends BaseEntity {

    private LocalDateTime fechaCreacionDeuda;
    private BigDecimal montoDeuda;
    private BigDecimal montoTotalDevuelto;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Reembolso reembolso;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DeudaEstablecimientoEstado> estados = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private DeudaEstablecimientoEstado estadoActual;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private Establecimiento establecimientoDeudor;

    /**
     * Realiza un cambio de estado de una deuda. <p></p>
     * Incluye la creación de nuevas instancias, relaciones y cambios en los atributos de las clases involucradas.
     * Incluye la actualización del monto total devuelto, pero no el cambio al estado correcto según monto devuelto.
     * @param estado Estado al que se quiere cambiar el pago
     * @param tiempoCambio Fecha y hora a la que se realizó el cambio
     */
    public void cambiarEstado(EstadoDeudaEstablecimiento estado, LocalDateTime tiempoCambio, BigDecimal montoDevueltoTransaccion, String motivo){
        // Al último estado le damos FechaHoraFin, si es que había uno (primer estado de la deuda)
        if (this.estadoActual != null)
            this.estadoActual.setFechaHoraFin(tiempoCambio);

        // Creamos la nueva DeudaEstablecimientoEstado
        DeudaEstablecimientoEstado nuevoDPE = new DeudaEstablecimientoEstado(tiempoCambio, null, montoDevueltoTransaccion, motivo, estado);

        // Actualizamos el monto total devuelto
        this.montoTotalDevuelto = this.montoTotalDevuelto.add(montoDevueltoTransaccion);

        // Agregamos la nueva ReembolsoEstado a las relaciones
        this.estadoActual = nuevoDPE;
        this.estados.add(nuevoDPE);
    }
}
