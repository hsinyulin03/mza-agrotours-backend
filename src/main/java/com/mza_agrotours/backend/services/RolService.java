package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.roles_permisos.*;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.roles_permisos.Permiso;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermiso;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.RolProtegido;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.EstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.RolError;
import com.mza_agrotours.backend.mappers.RolMapper;
import com.mza_agrotours.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class RolService {
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final RolMapper rolMapper;
    private final TipoPermisoRepository tipoPermisoRepository;
    private final PermisoRepository permisoRepository;
    private final ProductorRepository productorRepository;
    private final EstablecimientoRepository establecimientoRepository;

    public RolService(RolRepository rolRepository,
                      UsuarioRepository usuarioRepository,
                      AdministradorSistemasRepository administradorSistemasRepository,
                      RolMapper rolMapper,
                      TipoPermisoRepository tipoPermisoRepository,
                      PermisoRepository permisoRepository,
                      ProductorRepository productorRepository,
                      EstablecimientoRepository establecimientoRepository) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.rolMapper = rolMapper;
        this.tipoPermisoRepository = tipoPermisoRepository;
        this.permisoRepository = permisoRepository;
        this.productorRepository = productorRepository;
        this.establecimientoRepository = establecimientoRepository;

    }

    @Transactional(readOnly = true)
    public List<PermisoCodigo> obtenerPermisoCodigosAdminPorEmail(String email) {
        return this.administradorSistemasRepository.findPermisoCodigosByEmailActivo(email);
    }

    @Transactional(readOnly = true)
    public List<RolGetCatalogoDTO> obtenerRolesAdminCatalogo() {
        return this.obtenerRolesCatalogoByTipoPermisoNombre(RolScope.admin());
    }

    @Transactional
    public RolCreateResponse crearRolAdmin(RolCreateRequest rolCreateRequest) {
        Rol rolCreado = crearRol(rolCreateRequest, RolScope.admin());
        return this.rolMapper.rolToRolCreateResponse(rolCreado);
    }

    @Transactional
    public RolUpdateResponse modificarRolAdmin(String rolId, RolUpdateRequest rolUpdateRequest) {
        Rol rolModificado = modificarRol(rolId, rolUpdateRequest, RolScope.admin());
        return this.rolMapper.rolToRolUpdateResponse(rolModificado);
    }

    @Transactional
    public boolean bajaRolAdmin(String rolId) {
        return bajaRol(rolId, RolScope.admin());
    }


    //Gestión de roles de productor
    @Transactional(readOnly = true)
    public List<RolGetCatalogoDTO> obtenerRolesProductorCatalogo(UUID establecimientoId) {
        return this.obtenerRolesCatalogoByTipoPermisoNombre(RolScope.productor(establecimientoId));
    }

    @Transactional
    public RolCreateResponse crearRolProductor(UUID establecimientoId, RolCreateRequest rolCreateRequest) {
        Rol rolCreado = crearRol(rolCreateRequest, RolScope.productor(establecimientoId));
        return this.rolMapper.rolToRolCreateResponse(rolCreado);
    }

    @Transactional
    public RolUpdateResponse modificarRolProductor(UUID establecimientoId, String rolId, RolUpdateRequest rolUpdateRequest) {
        Rol rolModificado = modificarRol(rolId, rolUpdateRequest, RolScope.productor(establecimientoId));
        return this.rolMapper.rolToRolUpdateResponse(rolModificado);
    }

    @Transactional
    public boolean bajaRolProductor(UUID establecimientoId, String rolId) {
        return bajaRol(rolId, RolScope.productor(establecimientoId));
    }

    private Rol crearRol(RolCreateRequest rolCreateRequest, RolScope scope) {
        TipoPermiso tipoPermiso = this.tipoPermisoRepository
                .findByNombre(scope.tipoPermisoNombre())
                .orElseThrow(() -> new IllegalStateException("Tipo de permiso inválido"));

        if (this.rolRepository.existsByNombreAndTipoPermisoAndEstablecimiento(rolCreateRequest.getNombre(), scope.tipoPermisoNombre(), scope.establecimientoId())) {
            throw new AppException(RolError.ROL_ALREADY_EXISTS);
        }

        List<Permiso> permisos = this.permisoRepository
                .findByTipoPermisoAndCodigoIn(tipoPermiso, rolCreateRequest.getPermisos());

        if (permisos.size() != rolCreateRequest.getPermisos().size()) {
            throw new AppException(RolError.PERMISO_INVALIDO);
        }

        Rol nuevoRol = new Rol();
        nuevoRol.setNombre(rolCreateRequest.getNombre());
        nuevoRol.setDescripcion(rolCreateRequest.getDescripcion());
        nuevoRol.setEsProtegido(false);
        nuevoRol.setPermisos(permisos);
        nuevoRol.setTipoPermiso(tipoPermiso);
        nuevoRol.setEstablecimiento(obtenerEstablecimiento(scope));

        return this.rolRepository.save(nuevoRol);
    }

    private Rol modificarRol(String rolId, RolUpdateRequest rolUpdateRequest, RolScope scope) {
        TipoPermiso tipoPermiso = this.tipoPermisoRepository
                .findByNombre(scope.tipoPermisoNombre())
                .orElseThrow(() -> new IllegalStateException("Tipo de permiso inválido"));
        /** TODO para pensar
         * Actualmente se permite que el administrador modifique los
         * permisos para su propio rol, algo que podría ser un tiro
         * en el pié, creo que lo voy a evitar.
         *
         * Pero cualquier cosa, puede suceder tranquilamente el muchcacho
         * ponerse de acuerdo con otro y elevar el rol del otro, después el otro
         * el de él y así pueden adquirir más permisos de los que debería. Lo
         * que practicamente hacer que esto de gestionar roles sea inútil.
         *
         * Hay varias soluciones (enumerados arbitrariamente):
         * 1. Hacerselo un poco más difícil evitando que pueda modificar su propio rol
         * 2. Que sólo pueda asignar los permisos que él tenga
         * 3. Que solo el ADMIN_LIDER tenga permisos para gestionar los roles
         */


        Rol rol = this.rolRepository
                .findVigenteByIdScoped(
                        UUID.fromString(rolId),
                        scope.tipoPermisoNombre(),
                        getRolExcluidoByTipoPermisoNombre(scope.tipoPermisoNombre()),
                        scope.establecimientoId())
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));

        List<Permiso> permisos = this.permisoRepository
                .findByTipoPermisoAndCodigoIn(tipoPermiso, rolUpdateRequest.getPermisos());

        if (permisos.size() != rolUpdateRequest.getPermisos().size()) {
            throw new AppException(RolError.PERMISO_INVALIDO);
        }

        rol.setNombre(rolUpdateRequest.getNombre());
        rol.setDescripcion(rolUpdateRequest.getDescripcion());
        rol.setPermisos(permisos);
        return this.rolRepository.save(rol);
    }

    private boolean bajaRol(String rolId, RolScope scope) {
        String rolExcluido = getRolExcluidoByTipoPermisoNombre(scope.tipoPermisoNombre());

        Rol rol = this.rolRepository
                .findVigenteByIdScoped(UUID.fromString(rolId), scope.tipoPermisoNombre(), rolExcluido, scope.establecimientoId())
                .orElseThrow(() -> new AppException(RolError.NOT_FOUND));


        Integer cantidadUsers = obtenerCantidadUsuariosPorRol(rol);
        if (cantidadUsers > 0) {
            throw new AppException(RolError.BAJA_ROL_CON_USUARIOS);
        }

        rol.setFechaHoraBaja(LocalDateTime.now());
        this.rolRepository.save(rol);
        return true;
    }

    private String getRolExcluidoByTipoPermisoNombre(TipoPermisoNombre tipoPermisoNombre) {
        return switch (tipoPermisoNombre) {
            case ADMIN -> RolProtegido.ADMIN_LIDER.getNombre();
            case PRODUCTOR -> RolProtegido.PRODUCTOR_LIDER.getNombre();
            default -> throw new IllegalStateException("Unexpected value: " + tipoPermisoNombre);
        };
    }

    private List<RolGetCatalogoDTO> obtenerRolesCatalogoByTipoPermisoNombre(RolScope scope) {
        //TODO: te lista los roles vigentes, pero en las pantallas veo que también te muestra los dado de baja
        List<Rol> rolesByTipoPermiso = this.rolRepository
                .findVigentesEnScope(scope.tipoPermisoNombre(), scope.establecimientoId());

        List<RolGetCatalogoDTO> rolesCatalogo = new ArrayList<>();
        for (Rol rol : rolesByTipoPermiso) {
            RolGetCatalogoDTO rolGetCatalogoDTO = this.rolMapper.rolToRolGetCatalogoDTO(rol);

            Integer cantidadUsers = obtenerCantidadUsuariosPorRol(rol);
            rolGetCatalogoDTO.setCantidadUsuarios(cantidadUsers);

            rolesCatalogo.add(rolGetCatalogoDTO);
        }

        return rolesCatalogo;
    }

    private Integer obtenerCantidadUsuariosPorRol(Rol rol) {
        TipoPermisoNombre tipoPermisoNombre = rol.getTipoPermiso().getNombre();

        return switch (tipoPermisoNombre) {
            case ADMIN -> this.administradorSistemasRepository.countByRolAndFechaHoraBajaIsNull(rol);
            case PRODUCTOR -> this.productorRepository.countByRolAndFechaHoraBajaIsNull(rol);
            default -> throw new IllegalStateException("Unexpected value: " + tipoPermisoNombre);
        };
    }
    private Establecimiento obtenerEstablecimiento(RolScope scope) {
        return scope.establecimientoId() == null
                ? null
                : this.establecimientoRepository
                  .findByIdAndFechaHoraBajaIsNull(scope.establecimientoId())
                  .orElseThrow (() -> new EstablecimientoNotFoundException());
    }

    private record RolScope(TipoPermisoNombre tipoPermisoNombre, UUID establecimientoId) {
        public static RolScope admin() {
            return new RolScope(TipoPermisoNombre.ADMIN, null);
        }

        public static RolScope productor(UUID establecimientoId) {
            return new RolScope(TipoPermisoNombre.PRODUCTOR, Objects.requireNonNull(establecimientoId));
        }

    }

}
