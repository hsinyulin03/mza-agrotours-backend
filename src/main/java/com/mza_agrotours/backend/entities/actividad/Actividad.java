package com.mza_agrotours.backend.entities;

import com.mza_agrotours.backend.enums.EstadoActividad;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actividad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Actividad extends BaseEntity{
    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "cupos_max", nullable = false)
    private int  cuposMax;

    @Column(name = "fecha_hora_baja_act")
    private LocalDateTime fechaHoraBaja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoActividad estado;

    //TODO: Agregar relación con establecimiento
    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;*/

    // Paso 2: Inclusiones y FAQs
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadInclusiones> inclusiones = new ArrayList<>();

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadFAQ> faqs = new ArrayList<>();

    // Paso 3: Tarifas por Rango Etario
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadRangoEtario> actividadRangoEtarios = new ArrayList<>();


    // Paso 4: Disponibilidad
    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadLogAltas> logAltas = new ArrayList<>();


    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActividadDia> actividadesDia = new ArrayList<>();



    public void addFaq(ActividadFAQ faq) {
        if (this.faqs == null) {
            this.faqs = new ArrayList<>();
        }
        this.faqs.add(faq);
        // Indicamos a faq quién es su "padre"
        faq.setActividad(this);
    }

    public void addInclusion(ActividadInclusiones inclusion) {
        if (this.inclusiones == null) {
            this.inclusiones = new ArrayList<>();
        }
        this.inclusiones.add(inclusion);
        inclusion.setActividad(this);
    }
    public void addLogAlta(ActividadLogAltas logAlta) {
        if (this.logAltas == null) {
            this.logAltas = new ArrayList<>();
        }
        this.logAltas.add(logAlta);
        logAlta.setActividad(this);
    }

    public void addActividadDia(ActividadDia actividadDia) {
        if (this.actividadesDia == null) {
            this.actividadesDia = new ArrayList<>();
        }
        this.actividadesDia.add(actividadDia);
        actividadDia.setActividad(this);
    }

    /*public void addTarifa(ActividadRangoEtario tarifa) {
        if (this.tarifas == null) {
            this.tarifas = new ArrayList<>();
        }
        this.tarifas.add(tarifa);
        tarifa.setActividad(this);
    }*/
    //TODO-Falta relacion con fotos, con actividad log alta, calificacion, actividad dia, Tipocultivos
}
