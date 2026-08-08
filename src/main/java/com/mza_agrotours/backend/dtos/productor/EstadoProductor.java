package com.mza_agrotours.backend.dtos.productor;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.EstadoProductorNombre;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class EstadoProductor extends BaseEntity {
    @Enumerated(value = EnumType.STRING)
    private EstadoProductorNombre nombre;

    private LocalDateTime fechaHoraBaja;
}
