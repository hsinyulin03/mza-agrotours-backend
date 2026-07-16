package com.mza_agrotours.backend.entities.reservas;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.TipoIdentificacion;
import com.mza_agrotours.backend.entities.actividad.ActividadRangoEtario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaDetalle extends BaseEntity {
    @Column(nullable = false)
    private Integer renglon;

    @Column (nullable = false)
    private String nombre;

    @Column (nullable = false)
    private LocalDate fechaNacimiento;

    @Column (nullable = false)
    private String identificacion;

    @Column (nullable = false)
    private BigDecimal subtotal;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private TipoIdentificacion tipoIdentificacion;

    @ManyToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private ActividadRangoEtario actividadRangoEtario;
}
