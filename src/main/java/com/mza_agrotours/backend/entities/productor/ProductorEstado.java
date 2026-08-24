package com.mza_agrotours.backend.entities.productor;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
public class ProductorEstado extends BaseEntity {
    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(nullable = false) // TODO: find out length
    private String motivo;

    // Cuándo se cerró realmente este tramo. NULL siempre significa "tramo vigente",
    // para todos los estados sin excepción.
    private LocalDateTime fechaHoraFin;

    // Hasta cuándo se planificó que dure el tramo. Solo lo llevan las suspensiones;
    // es una intención a futuro, no el cierre efectivo (ver fechaHoraFin).
    private LocalDateTime fechaHoraFinPrevista;

    @ManyToOne
    private EstadoProductor estadoProductor;

}
