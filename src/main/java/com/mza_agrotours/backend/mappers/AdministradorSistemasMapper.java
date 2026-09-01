package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.administrador_sistemas.AdminSistemasGetDTO;
import com.mza_agrotours.backend.dtos.administrador_sistemas.ConteosPorEstablecimientoAdminDTO;
import com.mza_agrotours.backend.dtos.administrador_sistemas.EstablecimientoAdminDTO;
import com.mza_agrotours.backend.entities.AdministradorSistemas;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AdministradorSistemasMapper {

    List<EstablecimientoAdminDTO> establecimientoListToEstablecimientoAdminDTOList(List<Establecimiento> establecimientos, @Context ConteosPorEstablecimientoAdminDTO conteosPorEstablecimientoAdminDTO);

    @Mapping(target="productorLider", source="titular.usuario.nombre")
    @Mapping(target="departamento", source="departamento.nombre")
    @Mapping(target="estado", source="estadoActual.estadoEstablecimiento.nombre")
    @Mapping(target="motivoEstado", source="estadoActual.motivo")
    @Mapping(target="fechaEstado", source="estadoActual.fechaInicio")
    @Mapping(target="fechaAlta", source="fechaHoraAlta")
    EstablecimientoAdminDTO establecimientoToEstablecimientoAdminDTO(Establecimiento establecimiento);

    default List<AdminSistemasGetDTO> administradorSistemasListToAdminSistemasGetDTOList(List<AdministradorSistemas> administradorSistemas) {
        return administradorSistemas.stream().map(this::administradorSistemasToAdminSistemasGetDTO).toList();
    }

    default AdminSistemasGetDTO administradorSistemasToAdminSistemasGetDTO(AdministradorSistemas administradorSistemas) {
        AdminSistemasGetDTO adminSistemasGetDTO = new AdminSistemasGetDTO();
        adminSistemasGetDTO.setId(administradorSistemas.getId().toString());

        //Data usuario
        adminSistemasGetDTO.setNombreUsuario(administradorSistemas.getUsuario().getNombre());
        adminSistemasGetDTO.setEmailUsuario(administradorSistemas.getUsuario().getEmail());
        adminSistemasGetDTO.setIdentificacion(administradorSistemas.getUsuario().getIdentificacion());

        //Data rol
        adminSistemasGetDTO.setNombreRol(administradorSistemas.getRol().getNombre());
        adminSistemasGetDTO.setEsLider(administradorSistemas.getRol().getNombre().equals("Administrador Líder"));

        return adminSistemasGetDTO;
    }

    @AfterMapping
    default void llenarDatosEstablecimiento(@MappingTarget List<EstablecimientoAdminDTO> establecimientoDTOs, @Context ConteosPorEstablecimientoAdminDTO conteosPorEstablecimientoAdminDTO) {
        for (EstablecimientoAdminDTO establecimientoAdminDTO: establecimientoDTOs) {
            establecimientoAdminDTO.setCantidadActividadesPublicadas(conteosPorEstablecimientoAdminDTO
                    .getPublicacionesPorEstablecimiento()
                    .getOrDefault(UUID.fromString(establecimientoAdminDTO.getId()), 0L));
            establecimientoAdminDTO.setCantidadReservasHistorico(conteosPorEstablecimientoAdminDTO
                    .getReservasHistoricasPorEstablecimiento()
                    .getOrDefault(UUID.fromString(establecimientoAdminDTO.getId()), 0L));
        }
    }
}
