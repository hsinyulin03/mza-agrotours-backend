package com.mza_agrotours.backend.entities.receta;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.Dificultad;
import com.mza_agrotours.backend.enums.DuracionNombre;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Receta extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer porciones;

    @Column(name = "tiempo_mins_aprox", nullable = false)
    private int tiempoMinsAprox;

    private LocalDateTime fechaHoraBaja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Dificultad dificultad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duracion_id", nullable = false)
    private Duracion duracion;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "receta_id")
    private List<Paso> pasos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "receta_id")
    private List<Ingrediente> ingredientes = new ArrayList<>();
}
