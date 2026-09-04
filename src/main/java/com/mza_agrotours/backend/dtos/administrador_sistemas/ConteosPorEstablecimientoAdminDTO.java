package com.mza_agrotours.backend.dtos.administrador_sistemas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConteosPorEstablecimientoAdminDTO {
    private Map<UUID, Long> publicacionesPorEstablecimiento;
    private Map<UUID, Long> reservasHistoricasPorEstablecimiento;
}
