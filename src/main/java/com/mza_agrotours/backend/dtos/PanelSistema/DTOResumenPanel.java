package com.mza_agrotours.backend.dtos.PanelSistema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTOResumenPanel {
    long administradoresActivos;
    long rolesActivos;
    long establecimientosActivos;
    long establecimientosSuspendidos;
    List<EstablecimientoAdminDto> suspendidos;


}
