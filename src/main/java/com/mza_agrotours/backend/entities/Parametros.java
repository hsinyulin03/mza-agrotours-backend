package com.mza_agrotours.backend.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Parametros extends BaseEntity {
    String logoEmpresa;
    String nombreEmpresa;
    String monedaDefecto;       //TODO estamos ignorando las monedas por ahora
    String cvu;
    Integer diasMaxCrearActividad;
    Integer diasMinReembolso;
    Integer ttlReserva;         // En minutos
    Float porcentajeComision;
}