package com.mza_agrotours.backend.entities.incidencia;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Incidencia extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String titulo;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(name = "fecha_hora_incio")
    private LocalDateTime fechaHoraIncio;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "incidencia_id")
    private List<IncidenciaEstado> estados = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "estado_actual_id")
    private IncidenciaEstado estadoActual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}