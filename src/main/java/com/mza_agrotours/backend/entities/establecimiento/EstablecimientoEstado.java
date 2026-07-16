package com.mza_agrotours.backend.entities.establecimiento;
import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EstablecimientoEstado extends BaseEntity {

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "estado_id", nullable = false)
    private EstadoEstablecimiento estadoEstablecimiento;

}
