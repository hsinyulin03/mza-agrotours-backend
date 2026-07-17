package com.mza_agrotours.backend.entities.actividad;

import com.mza_agrotours.backend.entities.*;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Actividad extends BaseEntity {
    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "cupos_max", nullable = false)
    private int  cuposMax;

    @Column(name = "fecha_hora_baja_act")
    private LocalDateTime fechaHoraBaja;

    //TODO: No estoy segura de esta relación, por cómo está no guardo un historial, solo sé el estado que está actualmente
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoActividad estado;


    //TODO: Agregar relación con establecimiento
    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;*/

    // Paso 2: Inclusiones y FAQs
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "actividad_id")
    private List<ActividadInclusiones> inclusiones = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "actividad_id")
    private List<ActividadFAQ> faqs = new ArrayList<>();

    // Paso 3: Tarifas por Rango Etario
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "actividad_id")
    private List<ActividadRangoEtario> actividadRangoEtarios = new ArrayList<>();


    // Paso 4: Disponibilidad
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "actividad_id")
    private List<ActividadLogAltas> logAltas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "actividad_id")
    private List<ActividadDia> actividadesDias = new ArrayList<>();


    public void addFaq(ActividadFAQ faq) {
        this.faqs.add(faq);
    }

    public void addInclusion(ActividadInclusiones inclusion) {
        this.inclusiones.add(inclusion);
    }

    public void addActividadRangoEtario(ActividadRangoEtario actividadRangoEtarios) {
        this.actividadRangoEtarios.add(actividadRangoEtarios);
    }

    public void addLogAlta(ActividadLogAltas logAlta) {
        this.logAltas.add(logAlta);
    }

    public void addActividadDia(ActividadDia actividadDias) {
        this.actividadesDias.add(actividadDias);

    }
    //TODO: Falta relacion con fotos, calificacion, Tipocultivo
}
