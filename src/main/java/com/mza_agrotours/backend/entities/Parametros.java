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
public class Parametros extends BaseEntity {
    String logoEmpresa;
    String nombreEmpresa;
    String monedaDefecto;       //TODO estamos ignorando las monedas por ahora
    String cvu;                 // TODO quitar CVU: Agrotours cobra su comisión vía marketplace_fee de MP (requiere cambios en el front)
    Integer diasMaxCrearActividad;
    Integer diasMinReembolso;
    Integer ttlReserva;         // En minutos
    @Column(precision = 5, scale = 4)
    BigDecimal porcentajeComision;  // Fracción: 0.1 = 10%
}