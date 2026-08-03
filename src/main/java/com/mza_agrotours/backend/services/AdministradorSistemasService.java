package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasCreateReq;
import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasGetDTO;
import com.mza_agrotours.backend.dtos.administrador_sistemas.AdministradorSistemasUpdateReq;
import com.mza_agrotours.backend.dtos.roles_permisos.RolGetShortDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.roles_permisos.Rol;
import com.mza_agrotours.backend.entities.roles_permisos.TipoPermisoNombre;
import com.mza_agrotours.backend.enums.RolProtegido;
import com.mza_agrotours.backend.exceptions.AdministradorSistemasError;
import com.mza_agrotours.backend.exceptions.AppException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.AdministradorSistemasMapper;
import com.mza_agrotours.backend.mappers.RolMapper;
import com.mza_agrotours.backend.repositories.AdministradorSistemasRepository;
import com.mza_agrotours.backend.repositories.RolRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

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

    public AdministradorSistemasService(RolRepository rolRepository,
                                        AdministradorSistemasRepository administradorSistemasRepository,
                                        UsuarioRepository usuarioRepository,
                                        AdministradorSistemasMapper administradorSistemasMapper,
                                        RolMapper rolMapper) {
        this.rolRepository = rolRepository;
        this.administradorSistemasRepository = administradorSistemasRepository;
        this.usuarioRepository = usuarioRepository;
        this.administradorSistemasMapper = administradorSistemasMapper;
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

        // Te pensaste que iba a caer en esto silly boy,
        // NO VAS A PONER ROLES DE OTROS TIPO PERMISOS
        // Y NO VAS A AÑADIR A DOS ADMINISTRADORES LÍDERES
        Rol rolAdmin = this.rolRepository
                .findByIdAndTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(
                        adminSistemasCreateReq.getRolId(),
                        TipoPermisoNombre.ADMIN,
                        RolProtegido.ADMIN_LIDER.getNombre())
                .orElseThrow(() -> new AppException(AdministradorSistemasError.ROL_INVALIDO));

        //TODO: no podemos añadir a otro administrador líder
        // ...
        AdministradorSistemas administradorSistemas = new AdministradorSistemas();
        administradorSistemas.setUsuario(usuario);
        administradorSistemas.setRol(rolAdmin);
        administradorSistemas.setFechaHoraAlta(LocalDateTime.now());
        administradorSistemas = this.administradorSistemasRepository.save(administradorSistemas);

        return this.administradorSistemasMapper.administradorSistemasToAdminSistemasGetDTO(administradorSistemas);
    }

    public AdminSistemasGetDTO updateRolAdmin(UUID adminId, AdministradorSistemasUpdateReq administradorSistemasUpdateReq) {
        AdministradorSistemas administradorSistemas = this.administradorSistemasRepository
                .findByIdAndFechaHoraBajaIsNull(adminId)
                .orElseThrow(() -> new AppException(AdministradorSistemasError.NOT_FOUND));

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

        return this.administradorSistemasMapper.administradorSistemasToAdminSistemasGetDTO(administradorSistemas);
    }

    public List<RolGetShortDTO> obtenerRolesAdmin() {
        List<Rol> rolesAdmin = this.rolRepository
                .findByTipoPermiso_NombreAndFechaHoraBajaIsNullAndNombreIsNotContaining(
                        TipoPermisoNombre.ADMIN,
                        RolProtegido.ADMIN_LIDER.getNombre());
        return this.rolMapper.rolListToRolGetShortDTOList(rolesAdmin);
    }


}
