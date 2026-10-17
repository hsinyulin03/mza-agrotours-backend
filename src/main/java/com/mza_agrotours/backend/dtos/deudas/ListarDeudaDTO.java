package com.mza_agrotours.backend.dtos.deudas;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListarDeudaDTO(
        String id,
        LocalDateTime fechaCreacionDeuda,
        String establecimientoDeudorNombre,
        BigDecimal montoTotal,
        BigDecimal montoDevuelto,
        String estado
) {
}
