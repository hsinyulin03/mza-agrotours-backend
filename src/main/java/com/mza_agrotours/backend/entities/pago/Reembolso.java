package com.mza_agrotours.backend.entities.pago;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.reservas.Reserva;
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
public class Reembolso extends BaseEntity {
    private LocalDateTime fechaHoraReembolso;
    private LocalDateTime fechaHoraPedido;
    private BigDecimal montoReembolso;

    private String idReembolsoExterno;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private Reserva reserva;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private ReembolsoEstado estadoActual;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "reembolso_id")
    private List<ReembolsoEstado> estados= new ArrayList<>();

    @OneToOne
    private Pago pagoReembolsoSistema;

    /**
     * Realiza un cambio de estado de un reembolso. <p></p>
     * Incluye la creación de nuevas instancias, relaciones y cambios en los atributos de las clases involucradas.
     * No incluye el agregar un pago en caso de ser un reembolso por sistema.
     * @param estado Estado al que se quiere cambiar el pago
     * @param tiempoCambio Fecha y hora a la que se realizó el cambio
     */
    public void cambiarEstado(EstadoReembolso estado, LocalDateTime tiempoCambio){
        // Al último estado le damos FechaHoraFin, si es que había uno (primer estado del reembolso)
        if (this.estadoActual != null)
            this.estadoActual.setFechaHoraFin(tiempoCambio);

        // Creamos la nueva ReembolsoEstado
        ReembolsoEstado nuevoRE = new ReembolsoEstado(tiempoCambio, null, estado);

        // Agregamos la nueva ReembolsoEstado a las relaciones
        this.estadoActual = nuevoRE;
        this.estados.add(nuevoRE);
    }
}
