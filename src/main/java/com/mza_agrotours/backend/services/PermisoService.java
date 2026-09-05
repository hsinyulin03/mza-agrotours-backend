package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.roles_permisos.GrupoPermisoDTO;
import com.mza_agrotours.backend.entities.roles_permisos.GrupoPermiso;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.mappers.PermisoMapper;
import com.mza_agrotours.backend.repositories.GrupoPermisoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermisoService {
    private final GrupoPermisoRepository grupoPermisoRepository;
    private final PermisoMapper permisoMapper;

    public PermisoService(
            GrupoPermisoRepository grupoPermisoRepository,
            PermisoMapper permisoMapper) {
        this.grupoPermisoRepository = grupoPermisoRepository;
        this.permisoMapper = permisoMapper;
    }

    public List<GrupoPermisoDTO> obtenerPermisosAdmin() {
        return this.obtenerPermisosAgrupados(TipoPermisoNombre.ADMIN);
    }

    public List<GrupoPermisoDTO> obtenerPermisosProductor() {
        return this.obtenerPermisosAgrupados(TipoPermisoNombre.PRODUCTOR);
    }

    private List<GrupoPermisoDTO> obtenerPermisosAgrupados(TipoPermisoNombre tipoPermisoNombre) {
        List<GrupoPermiso> gruposPermisos = this.grupoPermisoRepository
                .findAllByTipoPermiso_Nombre(tipoPermisoNombre);
        return this.permisoMapper
                .grupoPermisoListToGrupoPermisoDTOList(gruposPermisos);
    }
}

