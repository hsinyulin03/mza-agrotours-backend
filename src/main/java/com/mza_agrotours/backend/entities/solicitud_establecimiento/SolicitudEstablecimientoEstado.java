package com.mza_agrotours.backend.entities.solicitud_establecimiento;

import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
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
    @Column(nullable = false)
    private LocalDateTime fechaHoraRevision;

    private String razonRevision;

    @ManyToOne(optional = false)
    private EstadoSolicitudEstablecimiento estadoSolicitudEstablecimiento;

    @ManyToOne
    private AdministradorSistemas revisor;
}
