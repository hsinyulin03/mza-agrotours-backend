package com.mza_agrotours.backend.entities.productor;

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
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductorEstado extends BaseEntity {
    @Column(nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(nullable = false) // TODO: find out length
    private String motivo;

    private LocalDateTime fechaHoraFin;

    @ManyToOne
    private EstadoProductor estadoProductor;

}
