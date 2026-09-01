package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.administrador_sistemas.*;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.enums.RolProtegido;
import com.mza_agrotours.backend.enums.TipoPermisoNombre;
import com.mza_agrotours.backend.exceptions.AdministradorSistemasError;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.AdministradorSistemasMapper;
import com.mza_agrotours.backend.mappers.RolMapper;
import com.mza_agrotours.backend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdministradorSistemasService {
    private final RolRepository rolRepository;
    private final AdministradorSistemasRepository administradorSistemasRepository;
    private final UsuarioRepository usuarioRepository;
    private final AdministradorSistemasMapper administradorSistemasMapper;
    private final RolMapper rolMapper;
    private final EstablecimientoRepository establecimientoRepository;
    private final EstadoEstablecimientoRepository estadoEstablecimientoRepository;

    public AdministradorSistemasService(RolRepository rolRepository,
                                        AdministradorSistemasRepository administradorSistemasRepository,
                                        UsuarioRepository usuarioRepository,
                                        AdministradorSistemasMapper administradorSistemasMapper,
                                        EstablecimientoRepository establecimientoRepository,
                                        EstadoEstablecimientoRepository estadoEstablecimientoRepository,
                                        RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasMapper = administradorSistemasMapper;
        this.establecimientoRepository = establecimientoRepository;
        this.estadoEstablecimientoRepository = estadoEstablecimientoRepository;
        this.rolMapper = rolMapper;
    }

    public List<AdminSistemasGetDTO> findAllAdminSistemasVigentes() {
        List<AdministradorSistemas> administradorSistemas = this.administradorSistemasRepository.findByFechaHoraBajaIsNull();
        return this.administradorSistemasMapper
                .administradorSistemasListToAdminSistemasGetDTOList(administradorSistemas);

    }

    public AdminSistemasGetDTO createAdmin(AdminSistemasCreateReq adminSistemasCreateReq) {
        Usuario usuario = usuarioRepository
                .findActiveByEmail(adminSistemasCreateReq.getEmailUsuario())
                .orElseThrow(() -> new UsuarioNotFound("No se encontró el usuario"));

        if (this.administradorSistemasRepository
                .existsByUsuarioAndFechaHoraBajaIsNull(usuario)) {
            throw new AppException(AdministradorSistemasError.ALREADY_EXISTS);
        }

        Rol rolAdmin = this.rolRepository
                .findByIdAndTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(
                        adminSistemasCreateReq.getRolId(),
                        TipoPermisoNombre.ADMIN,
                        RolProtegido.ADMIN_LIDER.getNombre())
                .orElseThrow(() -> new AppException(AdministradorSistemasError.ROL_INVALIDO));

        AdministradorSistemas administradorSistemas = new AdministradorSistemas();
        administradorSistemas.setUsuario(usuario);
        administradorSistemas.setRol(rolAdmin);
        administradorSistemas.setFechaHoraAlta(LocalDateTime.now());
        administradorSistemas = this.administradorSistemasRepository.save(administradorSistemas);

        // TODO: entidad que diga quien hizo el cambio
        return this.administradorSistemasMapper.administradorSistemasToAdminSistemasGetDTO(administradorSistemas);
    }

    public AdminSistemasGetDTO updateRolAdmin(UUID adminId,
                                              AdministradorSistemasUpdateReq administradorSistemasUpdateReq,
                                              String emailAdminEjecutor) {
        AdministradorSistemas administradorSistemas = this.administradorSistemasRepository
                .findByIdAndFechaHoraBajaIsNull(adminId)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.NOT_FOUND));

        validarNoEsAutoGestion(administradorSistemas.getUsuario().getEmail(), emailAdminEjecutor);

        if(administradorSistemas.getRol().getNombre().equals(RolProtegido.ADMIN_LIDER.getNombre())){
            throw new AppException(AdministradorSistemasError.LIDER_INMUTABLE);
        }

        // Mismas reglas que en el alta: solo roles de tipo permiso ADMIN
        // y nunca el rol de Administrador Líder
        Rol rolAdmin = this.rolRepository
                .findByIdAndTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(
                        administradorSistemasUpdateReq.getRolId(),
                        TipoPermisoNombre.ADMIN,
                        RolProtegido.ADMIN_LIDER.getNombre())
                .orElseThrow(() -> new AppException(AdministradorSistemasError.ROL_INVALIDO));

        administradorSistemas.setRol(rolAdmin);
        administradorSistemas = this.administradorSistemasRepository.save(administradorSistemas);
        // TODO: entidad que diga quien hizo el cambio
        return this.administradorSistemasMapper.administradorSistemasToAdminSistemasGetDTO(administradorSistemas);
    }

    public boolean deleteAdmin(UUID adminId, String emailAdminEjecutor) {
        AdministradorSistemas administradorSistemas = this.administradorSistemasRepository
                .findByIdAndFechaHoraBajaIsNull(adminId)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.NOT_FOUND));

        validarNoEsAutoGestion(administradorSistemas.getUsuario().getEmail(), emailAdminEjecutor);

        if (administradorSistemas.getRol().getNombre().equals(RolProtegido.ADMIN_LIDER.getNombre())) {
            throw new AppException(AdministradorSistemasError.LIDER_INMUTABLE);
        }
        // TODO: entidad que diga quien hizo el cambio
        administradorSistemas.setFechaHoraBaja(LocalDateTime.now());
        this.administradorSistemasRepository.save(administradorSistemas);
        return true;
    }

    public List<RolGetShortDTO> obtenerRolesAdmin() {
        List<Rol> rolesAdmin = this.rolRepository
                .findByTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(
                        TipoPermisoNombre.ADMIN,
                        RolProtegido.ADMIN_LIDER.getNombre());
        return this.rolMapper.rolListToRolGetShortDTOList(rolesAdmin);
    }

    @Transactional(readOnly = true)
    public List<EstablecimientoAdminDTO> obtenerEstablecimientos() {
        List<Establecimiento> establecimientos = this.establecimientoRepository.findByFechaHoraBajaIsNull();
        return this.administradorSistemasMapper
                .establecimientoListToEstablecimientoAdminDTOList(establecimientos);
    }

    public EstablecimientoAdminDTO suspenderEstablecimiento(UUID establecimientoId, EstablecimientoSuspenderReq establecimientoSuspenderReq) {
        Establecimiento establecimiento = this.establecimientoRepository
                .findByIdAndFechaHoraBajaIsNull(establecimientoId)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.ESTABLECIMIENTO_NOT_FOUND));

        if (!establecimiento.getEstadoActual().getEstadoEstablecimiento().getNombre().equals(EstadoEstablecimientoNombre.ACTIVO)) {
            throw new AppException(AdministradorSistemasError.ESTABLECIMIENTO_NO_ACTIVO);
        }

        EstadoEstablecimiento estadoSuspendido = obtenerEstadoEstablecimientoByNombre(EstadoEstablecimientoNombre.SUSPENDIDO);
        establecimiento.cambiarEstado(estadoSuspendido, establecimientoSuspenderReq.getMotivo(), LocalDateTime.now());

        establecimiento = this.establecimientoRepository.save(establecimiento);
        return this.administradorSistemasMapper.establecimientoToEstablecimientoAdminDTO(establecimiento);
    }

    public EstablecimientoAdminDTO reactivarEstablecimiento(UUID establecimientoId) {
        Establecimiento establecimientoSuspendido = this.establecimientoRepository
                .findByIdAndFechaHoraBajaIsNull(establecimientoId)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.ESTABLECIMIENTO_NOT_FOUND));

        if (!establecimientoSuspendido.getEstadoActual().getEstadoEstablecimiento().getNombre().equals(EstadoEstablecimientoNombre.SUSPENDIDO)) {
            throw new AppException(AdministradorSistemasError.ESTABLECIMIENTO_NO_SUSPENDIDO);
        }

        EstadoEstablecimiento estadoActivo = obtenerEstadoEstablecimientoByNombre(EstadoEstablecimientoNombre.ACTIVO);
        establecimientoSuspendido.cambiarEstado(estadoActivo, "Reactivación de establecimiento", LocalDateTime.now());

        establecimientoSuspendido = this.establecimientoRepository.save(establecimientoSuspendido);
        return this.administradorSistemasMapper.establecimientoToEstablecimientoAdminDTO(establecimientoSuspendido);
    }

    // Nadie gestiona su propio rol de administrador: ni para escalarlo ni para darse de baja
    private void validarNoEsAutoGestion(String emailUsuarioAfectado, String emailAdminEjecutor) {
        if (emailUsuarioAfectado.equalsIgnoreCase(emailAdminEjecutor)) {
            throw new AppException(AdministradorSistemasError.AUTO_GESTION_PROHIBIDA);
        }
    }

    // Los estados de establecimiento vienen del seeder: si falta, es un problema de configuracion
    private EstadoEstablecimiento obtenerEstadoEstablecimientoByNombre(EstadoEstablecimientoNombre estadoNombre) {
        return this.estadoEstablecimientoRepository
                .findByNombreAndFechaBajaIsNull(estadoNombre)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.ESTADO_ESTABLECIMIENTO_NO_CONFIGURADO));
    }


}
