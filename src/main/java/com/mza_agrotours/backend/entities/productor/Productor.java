package com.mza_agrotours.backend.entities.productor;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Productor extends BaseEntity {
    @Column(nullable = false)
    private LocalDateTime fechaHoraAlta;

    private LocalDateTime fechaHoraBaja;

    @ManyToOne(optional = false)
    private Establecimiento establecimiento;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Rol rol;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "productor_id")
    private List<ProductorEstado> estados = new ArrayList<>();

    @ManyToOne(optional = false)
    private EstadoProductor estadoActual;

    /**
     * Cierra el tramo vigente (si lo hay) y abre uno nuevo, manteniendo el invariante
     * de que a lo sumo un ProductorEstado tiene fechaHoraFin == null.
     *
     * @param fechaHoraFinPrevista hasta cuándo se planifica que dure el tramo; null si es indefinido.
     */
    public void cambiarEstado(EstadoProductor estado,
                              String motivo,
                              LocalDateTime tiempoCambio,
                              LocalDateTime fechaHoraFinPrevista) {
        this.estados.stream()
                .filter(tramo -> tramo.getFechaHoraFin() == null)
                .forEach(tramo -> tramo.setFechaHoraFin(tiempoCambio));

        ProductorEstado nuevoTramo = new ProductorEstado();
        nuevoTramo.setFechaHoraInicio(tiempoCambio);
        nuevoTramo.setMotivo(motivo);
        nuevoTramo.setFechaHoraFin(null);
        nuevoTramo.setFechaHoraFinPrevista(fechaHoraFinPrevista);
        nuevoTramo.setEstadoProductor(estado);

        this.estados.add(nuevoTramo);
        this.estadoActual = estado;
    }
}
