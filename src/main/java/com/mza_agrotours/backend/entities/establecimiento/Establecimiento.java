package com.mza_agrotours.backend.entities.establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.entities.productor.Productor;
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

    @OneToMany(mappedBy = "establecimiento")
    private List<Actividad> actividades = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "establecimiento_id")
    private List<EstablecimientoEstado> estados = new ArrayList<>();

    @ManyToOne
    private Productor titular;

    // Cultivos del establecimento
    @ManyToMany
    @JoinTable(
            name = "establecimiento_tipo_cultivo",
            joinColumns = @JoinColumn(name = "establecimiento_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_cultivo_id")
    )
    private List<TipoCultivo> tiposCultivos = new ArrayList<>();
    @OneToOne
    @JoinColumn(name = "estado_actual_id")
    private EstablecimientoEstado estadoActual;

    public void cambiarEstado(EstadoEstablecimiento estado, String motivo, LocalDateTime tiempoCambio) {
        this.estados.stream()
                .filter(tramo -> tramo.getFechaFin() == null)
                .forEach(tramo -> tramo.setFechaFin(tiempoCambio));

        EstablecimientoEstado nuevoTramo = new EstablecimientoEstado();
        nuevoTramo.setFechaInicio(tiempoCambio);
        nuevoTramo.setMotivo(motivo);
        nuevoTramo.setEstadoEstablecimiento(estado);
        nuevoTramo.setFechaFin(null);

        this.estados.add(nuevoTramo);
        this.estadoActual = nuevoTramo;
    }
}