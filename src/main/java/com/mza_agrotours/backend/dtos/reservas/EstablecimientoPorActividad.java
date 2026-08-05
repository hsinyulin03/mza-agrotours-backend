package com.mza_agrotours.backend.dtos.reservas;

import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;

import java.util.UUID;

public record EstablecimientoPorActividad(
        UUID actividadID,
        Establecimiento establecimiento
) {
}
