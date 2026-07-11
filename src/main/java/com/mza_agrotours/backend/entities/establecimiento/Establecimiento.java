package com.mza_agrotours.backend.entities.establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.TipoCultivo;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

    private String razonSocial;
    private Long cuit;
    private LocalDateTime fechaHoraBaja;
    private String descripcion;
    private String telefono;
    private String email;
    private String ubicacion;
    private String cvu;

    @ManyToOne
    @JoinColumn(name = "departamento_id")
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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "establecimiento_id")
    private List<TipoCultivo> tiposCultivos = new ArrayList<>();
}