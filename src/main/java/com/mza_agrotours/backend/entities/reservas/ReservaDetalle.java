package com.mza_agrotours.backend.entities.reservas;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.cuenta.TipoIdentificacion;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class ReservaDetalle extends BaseEntity {
    private Integer renglon;
    private String nombre;
    private LocalDate fechaNacimiento;
    private String identificacion;
    @Enumerated
    private TipoIdentificacion tipoIdentificacion;
    private Float subtotal;

    @ManyToOne
    private Reserva reserva;
    //TODO relación ActividadRangoEtario
}
