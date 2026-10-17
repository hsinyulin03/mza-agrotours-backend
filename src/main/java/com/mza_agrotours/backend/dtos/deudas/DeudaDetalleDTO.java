package com.mza_agrotours.backend.dtos.deudas;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DeudaDetalleDTO(
        String id,
        LocalDateTime fechaCreacionDeuda,
        String establecimientoDeudorNombre,
        BigDecimal montoTotal,
        BigDecimal montoDevuelto,
        List<DeudaEstadoDTO> estados
) {
}
