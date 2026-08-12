package com.mza_agrotours.backend.entities.solicitud_establecimiento;

import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SolicitudEstablecimiento extends BaseEntity {
    @Column(nullable = false, length = 100)
    private String nombreEstablecimiento;

    @Column(nullable = false)
    private LocalDateTime fechaHoraAlta;

    private LocalDateTime fechaHoraBaja;

    @Column(nullable = false, length = 100)
    private String razonSocial;

    @Column(nullable = false, length = 11)
    private String cuit;

    //@Column(nullable = false, length = 2000)
    //private String descripcionEstablecimiento;

    @Column(nullable = false, length = 200)
    private String domicilioLegal;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 16)
    private String telefono;

    @Column(nullable = false, length = 22)
    private String cvu;

    @ManyToOne(optional = false)
    private Departamento departamento;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "solicitud_establecimiento_id")
    private List<SolicitudEstablecimientoEstado> estados = new ArrayList<>();

    @OneToOne
    private SolicitudEstablecimientoEstado estadoActual;

    @OneToOne
    private Establecimiento establecimientoCreado;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Archivo> pruebas = new ArrayList<>();

}
