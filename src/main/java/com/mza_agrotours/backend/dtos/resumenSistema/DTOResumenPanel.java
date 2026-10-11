package com.mza_agrotours.backend.dtos.resumenSistema;

import java.util.List;

public class DTOResumenPanel {
    long administradoresActivos;
    long rolesActivos;
    long establecimientosActivos;
    long establecimientosSuspendidos;
    List<EstablecimientoAdminDto> suspendidos;

    public DTOResumenPanel(long administradoresActivos, long rolesActivos, long establecimientosActivos,
                           long establecimientosSuspendidos, List<EstablecimientoAdminDto> suspendidos) {
        this.administradoresActivos = administradoresActivos;
        this.rolesActivos = rolesActivos;
        this.establecimientosActivos = establecimientosActivos;
        this.establecimientosSuspendidos = establecimientosSuspendidos;
        this.suspendidos = suspendidos;
    }
}
