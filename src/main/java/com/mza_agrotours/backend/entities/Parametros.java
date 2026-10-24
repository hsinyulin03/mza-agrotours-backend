package com.mza_agrotours.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
// Tiene cargados los valores por defecto
public class Parametros extends BaseEntity {
    Integer diasMaxCrearActividad = 120;
    Integer diasMinReembolso = 3;
    Integer ttlReserva = 30;         // En minutos
    @Column(precision = 5, scale = 4)
    BigDecimal porcentajeComision = BigDecimal.valueOf(0.05);  // Fracción: 0.1 = 10%
    Integer diasImpagoReserva = 30; // Días que pasan hasta que una reserva que el productor debe pagar pase a impaga y sea pagada por un administrador de sistemas
}