package com.mza_agrotours.backend.dtos.estadisticasproductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarraDTO {
    String label;          // "Sem 1", "Ene", etc.
    int value;              // cantidad de reservas confirmadas en ese sub-período
    BigDecimal ganancia;     // suma de montoPagado en ese sub-período
}
