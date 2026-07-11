package com.mza_agrotours.backend.entities.reservas;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Reserva extends BaseEntity {
    private LocalDateTime fechaHoraReserva;
    private LocalDateTime fechaHoraFin;             // TODO wtf es fechaHoraFin
    private Float subtotalComisionTransaccion;      // Comisión al servicio de pagos
    private Float subTotalComisionPropia;           // Comisión que nos quedamos
    private Float subTotalProductor;                // Lo que queda al productor
    private Float totalReserva;                     // Monto total de la reserva

    //TODO relaciones - Visitante, Calificacion, Pago, Reembolso
    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ReservaEstado> estados = new ArrayList<>();

    @OneToOne(fetch = FetchType.EAGER)
    private ReservaEstado estadoActual;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservaDetalle> reservaDetalles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Actividad actividad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ActividadDia actividadDia;
}
