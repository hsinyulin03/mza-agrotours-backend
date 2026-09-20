package com.mza_agrotours.backend.dtos.actividad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class DTOReservasBloqueantes {
    private long pendientes;
    private long pagadas;

    public long getTotal() {
        return pendientes + pagadas;
    }
}
