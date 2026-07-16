package com.mza_agrotours.backend.entities.reservas;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.Visitante;
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
public class Reserva extends BaseEntity {
    @Column (nullable = false)
    private LocalDateTime fechaHoraInicio;          // FH cuando se inicia el camino de Reserva

    @Column (nullable = false)
    private LocalDateTime fechaHoraFin;             // FH cuando la Reserva llega a un estado final

    @Column (nullable = false)
    private BigDecimal subtotalComisionTransaccion;      // Comisión al servicio de pagos

    @Column (nullable = false)
    private BigDecimal subTotalComisionPropia;           // Comisión que nos quedamos

    @Column (nullable = false)
    private BigDecimal subTotalProductor;                // Lo que queda al productor

    @Column (nullable = false)
    private BigDecimal totalReserva;                     // Monto total de la reserva

    //TODO relaciones - Visitante, Calificacion, Pago, Reembolso
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private ReservaEstado estadoActual;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReservaEstado> estados = new ArrayList<>();

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReservaDetalle> reservaDetalles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private ActividadDia actividadDia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Visitante visitante;

    // Métodos

    public void addDetalle(ReservaDetalle reservaDetalle){
        reservaDetalles.add(reservaDetalle);
        reservaDetalle.setReserva(this);
    }

    /**
     * Realiza un cambio de estado de una reserva. <p></p>
     * Incluye la creación de nuevas instancias, relaciones y cambios en los atributos de las clases involucradas.
     * @param estado Estado al que se quiere cambiar la reserva
     * @param tiempoCambio Fecha y hora a la que se realizó el cambio
     */
    public void cambiarEstado(EstadoReserva estado, LocalDateTime tiempoCambio){
        // Al último estado le damos FechaHoraFin
        this.estadoActual.setFechaHoraFin(tiempoCambio);

        // Creamos la nueva ReservaEstado
        ReservaEstado nuevoRE = new ReservaEstado(tiempoCambio, null, estado, this);

        // Si es estado final le ponemos FechaHoraFin
        if (estado.getNombre().isEsFinal())
            this.fechaHoraFin = tiempoCambio;

        // Agregamos la nueva ReservaEstado a las relaciones
        this.estadoActual = nuevoRE;
        this.estados.add(nuevoRE);
    }
}
