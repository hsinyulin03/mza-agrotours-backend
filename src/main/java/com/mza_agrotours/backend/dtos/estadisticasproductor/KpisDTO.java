package com.mza_agrotours.backend.dtos.estadisticasproductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KpisDTO {
    int ocupacionFilled;     // reservas confirmadas en el rango
    int ocupacionTotal;        // cupos ofertados en el rango
    int ocupacionPct;

    BigDecimal beneficios;         // suma de montoPagado, reservas confirmadas
    Integer beneficiosDelta;     // round((actual - anterior) / anterior * 100), null si anterior == 0

    int cancelacionCount;     // reservas canceladas en el rango
    int cancelacionPct;       // round(canceladas / totalReservas * 100)
}
