package com.mza_agrotours.backend.entities.establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.TipoCultivo;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Establecimiento extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String razonSocial;

    @Column(nullable = false, length = 11)
    private String cuit;

    private LocalDateTime fechaHoraBaja;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(nullable = false, length = 16)
    private String telefono;

    @Column(nullable = false, length = 100)
    private String email;
    private String ubicacion;

    @Column(nullable = false, length = 22)
    private String cvu;

    @ManyToOne
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "establecimiento_id")
    private List<Actividad> actividades = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "establecimiento_id")
    private List<EstablecimientoEstado> estados = new ArrayList<>();

//    @ManyToOne
//    @JoinColumn(name = "productor_titular_id")
//    private Productor titular;

    // Cultivos del establecimento
    @ManyToMany
    @JoinTable(
            name = "establecimiento_tipo_cultivo",
            joinColumns = @JoinColumn(name = "establecimiento_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_cultivo_id")
    )
    private List<TipoCultivo> tiposCultivos = new ArrayList<>();
}