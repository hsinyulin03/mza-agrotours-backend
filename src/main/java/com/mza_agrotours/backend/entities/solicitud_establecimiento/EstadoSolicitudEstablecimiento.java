package com.mza_agrotours.backend.entities.solicitud_establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EstadoSolicitudEstablecimiento extends BaseEntity {
    private LocalDateTime fechaHoraBaja;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitudEstablecimientoNombre nombre;
}
