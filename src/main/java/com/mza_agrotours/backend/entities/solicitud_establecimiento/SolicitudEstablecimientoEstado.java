package com.mza_agrotours.backend.entities.solicitud_establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SolicitudEstablecimientoEstado extends BaseEntity {
    private LocalDateTime fechaHoraRevision;
    private String razonRevision;
}
