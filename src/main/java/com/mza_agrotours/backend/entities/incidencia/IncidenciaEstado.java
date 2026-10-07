package com.mza_agrotours.backend.entities.incidencia;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaEstado extends BaseEntity {

    @Column(name = "fecha_hora_incio")
    private LocalDateTime fechaHoraIncio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Column(nullable = false, length = 500)
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoIncidencia estado;
}