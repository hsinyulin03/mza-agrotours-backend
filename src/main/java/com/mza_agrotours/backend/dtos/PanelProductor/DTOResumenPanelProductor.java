package com.mza_agrotours.backend.dtos.PanelProductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOResumenPanelProductor {
    private PendientesDTO pendientes;
    private List<GrupoDiaReservasDTO> proximasReservas;
    private ReseniasDTO resenias;
}
