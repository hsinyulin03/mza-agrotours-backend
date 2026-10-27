package com.mza_agrotours.backend.dtos.actividad;

import java.math.BigDecimal;

// Rango [desde, hasta): desde null = sin piso, hasta null = sin techo
public record FiltroRangoPrecio(BigDecimal desde, BigDecimal hasta, long cantidad) {
}
