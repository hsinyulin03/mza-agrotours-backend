package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.solicitud_establecimiento.*;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimientoEstado;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SolicitudEstablecimientoMapper {
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "estados", ignore = true)
    @Mapping(target = "estadoActual", ignore = true)
    @Mapping(target = "departamento", ignore = true)
    SolicitudEstablecimiento solicitudEstablecimientoDtoToSolicitudEstablecimiento(SolicitudEstablecimientoCreateReq solicitudEstablecimientoCreateReq);

    List<SolicitudEstablecimientoUserShotDTO> solicitudEstablecimientosToSolicitudEstablecimientoShortDTOs(List<SolicitudEstablecimiento> solicitudEstablecimientos);

    @Mapping(target="estado", source = "estadoActual.estadoSolicitudEstablecimiento.nombre")
    SolicitudEstablecimientoUserShotDTO solicitudEstablecimientoToSolicitudEstablecimientoUserShortDTO(SolicitudEstablecimiento solicitudEstablecimiento);

    List<SolicitudEstablecimientoShortDTO> solicitudEstablecimientoToShortDTOs(List<SolicitudEstablecimiento> solicitudEstablecimientos);

    @Mapping(target = "departamento", source = "departamento.nombre")
    @Mapping(target = "nombreSolicitante", source = "usuario.nombre")
    @Mapping(target = "estado", source = "estadoActual.estadoSolicitudEstablecimiento.nombre")
    SolicitudEstablecimientoShortDTO solicitudEstablecimientoToSolicitudEstablecimientoShortDTO(SolicitudEstablecimiento solicitudEstablecimiento);

    @Mapping(target="estado", source = "estadoActual.estadoSolicitudEstablecimiento.nombre")
    @Mapping(target="departamento", source = "departamento.nombre")
    SolicitudEstablecimientoDTO solicitudEstablecimientoToDTO(SolicitudEstablecimiento solicitudEstablecimiento);

    @Mapping(target = "estado", source = "estadoActual.estadoSolicitudEstablecimiento.nombre")
    @Mapping(target = "departamento", source = "departamento.nombre")
    @Mapping(target = "nombreSolicitante", source = "usuario.nombre")
    @Mapping(target = "emailSolicitante", source = "usuario.email")
    @Mapping(target = "identificacionSolicitante", source = "usuario.identificacion")
    @Mapping(target = "fechaHoraAltaSolicitante", source = "usuario.fechaHoraAlta")
    SolicitudEstAdminDetalleDTO solicitudEstablecimientoToDTOAdmin(SolicitudEstablecimiento solicitudEstablecimiento);

    // TODO: revisar
    @Mapping(target="estado", source = "estadoSolicitudEstablecimiento.nombre")
    @Mapping(target="fecha", source = "fechaHoraRevision")
    @Mapping(target="observaciones", source = "razonRevision")
    @Mapping(target="revisor", source = "revisor.usuario.nombre")
    SolicitudEstablecimientoEstadoDTO solicitudEstablecimientoEstadoToDTO(SolicitudEstablecimientoEstado solicitudEstablecimientoEstado);

    SolicitudEstablecimientoPruebaDTO archivoToPruebaDTO(Archivo archivo);

    @Mapping(target = "nombre", source = "nombreEstablecimiento")
    @Mapping(target = "ubicacion", source = "domicilioLegal")
    @Mapping(target = "estados", ignore = true)
    @Mapping(target = "estadoActual", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "descripcion", constant = "")
    Establecimiento solicitudEstablecimientoToEstablecimiento(SolicitudEstablecimiento solicitudEstablecimiento);
}
