package com.mza_agrotours.backend.dtos.estadisticasproductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActividadPerformanceDTO {
    UUID id;
    String nombre;
    String cultivo;
    int cupos;
    int reservas;
    int ocupacion;          // round(reservas / cupos * 100), 0 si cupos == 0
    BigDecimal ingresos;
}
