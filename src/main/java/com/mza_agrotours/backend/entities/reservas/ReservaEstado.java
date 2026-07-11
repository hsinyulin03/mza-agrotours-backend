package com.mza_agrotours.backend.entities.reservas;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ReservaEstado extends BaseEntity {
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    // Relaciones
    @Enumerated
    private EstadoReserva estadoReserva;
    @ManyToOne(optional = false)
    private Reserva reserva;
}
