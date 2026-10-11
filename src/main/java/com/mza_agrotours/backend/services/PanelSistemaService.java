package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.PanelSistema.DTOResumenPanel;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PanelSistemaService {
    @Autowired
    private AdministradorSistemasRepository administradores;
    @Autowired
    private RolRepository roles;
    @Autowired
    private EstablecimientoRepository establecimientos;

    public DTOResumenPanel obtenerResumen() {
        return new DTOResumenPanel(
                administradores.countByFechaHoraBajaIsNull(),
                roles.countByTipoPermiso_NombreAndFechaHoraBajaIsNull(TipoPermisoNombre.ADMIN),
                establecimientos.countByEstadoNombre(EstadoEstablecimientoNombre.ACTIVO),
                establecimientos.countByEstadoNombre(EstadoEstablecimientoNombre.SUSPENDIDO),
                establecimientos.listarSuspendidos());
    }
}
