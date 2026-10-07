package com.mza_agrotours.backend.entities.establecimiento;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Archivo;
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
import java.util.Optional;

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

    @Column(nullable = false)
    private LocalDateTime fechaHoraAlta;

    private LocalDateTime fechaHoraBaja;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(nullable = false, length = 16)
    private String telefono;

    @Column(nullable = false, length = 100)
    private String email;
    private String ubicacion;

    // TODO quitar CVU: el cobro se hace con la CuentaMercadoPago vinculada por OAuth (requiere cambios en el front)
    @Column(nullable = false, length = 22)
    private String cvu;

    @ManyToOne
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamento;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "foto_id")
    private Archivo foto;

    @OneToMany(mappedBy = "establecimiento")
    private List<Actividad> actividades = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "establecimiento_id")
    private List<EstablecimientoEstado> estados = new ArrayList<>();

    @ManyToOne
    private Productor titular;

    // Historial de cuentas de MP vinculadas; a lo sumo una vigente (fechaHoraBaja == null)
    @OneToMany(mappedBy = "establecimiento", cascade = CascadeType.ALL)
    private List<CuentaMercadoPago> cuentasMercadoPago = new ArrayList<>();

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

    public void cambiarEstado(EstadoEstablecimiento estado, String motivo) {
        cambiarEstado(estado, motivo, null);
    }

    public void cambiarEstado(EstadoEstablecimiento estado, String motivo,AdministradorSistemas adminEjecutor) {
        LocalDateTime tiempoCambio = LocalDateTime.now();

        this.estados.stream()
                .filter(tramo -> tramo.getFechaFin() == null)
                .forEach(tramo -> tramo.setFechaFin(tiempoCambio));

        EstablecimientoEstado nuevoTramo = new EstablecimientoEstado();
        nuevoTramo.setFechaInicio(tiempoCambio);
        nuevoTramo.setMotivo(motivo);
        nuevoTramo.setEstadoEstablecimiento(estado);
        nuevoTramo.setFechaFin(null);
        nuevoTramo.setEjecutor(adminEjecutor);

        this.estados.add(nuevoTramo);
        this.estadoActual = nuevoTramo;
    }

    public Optional<CuentaMercadoPago> getCuentaMercadoPagoVigente() {
        return this.cuentasMercadoPago.stream()
                .filter(CuentaMercadoPago::isVigente)
                .findFirst();
    }

    /**
     * Da de baja la cuenta de MP vigente (si la hay) y vincula la nueva, manteniendo el invariante
     * de que a lo sumo una CuentaMercadoPago tiene fechaHoraBaja == null.
     */
    public void vincularCuentaMercadoPago(CuentaMercadoPago nuevaCuenta, LocalDateTime tiempoCambio) {
        getCuentaMercadoPagoVigente().ifPresent(cuenta -> cuenta.setFechaHoraBaja(tiempoCambio));

        nuevaCuenta.setEstablecimiento(this);
        nuevaCuenta.setFechaHoraAlta(tiempoCambio);
        nuevaCuenta.setFechaHoraBaja(null);
        this.cuentasMercadoPago.add(nuevaCuenta);
    }
}