package com.mza_agrotours.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Calificacion extends BaseEntity{
    @Column(nullable = false)
    private int puntaje;    // Estrellas de 1 a 5

    @Column(length = 2000)
    private String resenia;

    @Column(nullable = false)
    private LocalDateTime fechaHoraCalificacion;

}
