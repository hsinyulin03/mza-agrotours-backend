package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.SolicitudEstablecimientoShortDTO;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.dtos.solicitud_establecimiento.SolicitudEstablecimientoCreateResp;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimientoNombre;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimientoEstado;
import com.mza_agrotours.backend.exceptions.*;
import com.mza_agrotours.backend.mappers.ArchivoMapper;
import com.mza_agrotours.backend.mappers.SolicitudEstablecimientoMapper;
import com.mza_agrotours.backend.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitudEstablecimientoService {
    private final List<String> EXTENSIONES_VALIDAS = List.of("pdf", "jpg", "jpeg", "png");

    private final SolicitudEstablecimientoRepository solicitudEstablecimientoRepository;
    private final SolicitudEstablecimientoMapper solicitudEstablecimientoMapper;
    private final EstadoSolicitudEstablecimientoRepository estadoSolicitudEstablecimientoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstablecimientoRepository establecimientoRepository;
    private final ArchivoService archivoService;
    private final ArchivoMapper archivoMapper;

    public SolicitudEstablecimientoService(SolicitudEstablecimientoRepository solicitudEstablecimientoRepository,
                                           SolicitudEstablecimientoMapper solicitudEstablecimientoMapper,
                                           EstadoSolicitudEstablecimientoRepository estadoSolicitudEstablecimientoRepository,
                                           DepartamentoRepository departamentoRepository,
                                           UsuarioRepository usuarioRepository,
                                           EstablecimientoRepository establecimientoRepository,
                                           ArchivoService archivoService,
                                           ArchivoMapper archivoMapper) {
        this.solicitudEstablecimientoRepository = solicitudEstablecimientoRepository;
        this.solicitudEstablecimientoMapper = solicitudEstablecimientoMapper;
        this.estadoSolicitudEstablecimientoRepository = estadoSolicitudEstablecimientoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.establecimientoRepository = establecimientoRepository;
        this.archivoService = archivoService;
        this.archivoMapper = archivoMapper;
    }

    @Transactional
    public SolicitudEstablecimientoCreateResp crearSolicitudEstablecimiento(
            SolicitudEstablecimientoCreateReq solicitudEstablecimientoCreateReq,
            String emailUsuario)
            throws Exception {
        // -1. Encontrar si existen establecimientos vigentes con el cuit de la solicitud
        if (establecimientoRepository.existsByCuitAndFechaHoraBajaIsNull(solicitudEstablecimientoCreateReq.getCuit())) {
            throw new AppException(SolicitudEstablecimientoError.ESTABLECIMIENTO_ALREADY_EXISTS);
        }

        // 1. Encontrar si el usuario solicitante ya tiene solicitudes pendientes para este establecimiento (por razón social)
        Usuario usuarioSolicitante = this.usuarioRepository.findActiveByEmail(emailUsuario).orElseThrow(() -> new UsuarioNotFound("No se pudo encontrar el usuario " + emailUsuario));

        EstadoSolicitudEstablecimiento estadoPendiente = this.estadoSolicitudEstablecimientoRepository
                .findByNombre(EstadoSolicitudEstablecimientoNombre.PENDIENTE)
                .orElseThrow(() -> new EstadoSolicitudEstablecimientoNotFoundException("No se pudo encontrar el estado pendiente"));

        if (this.solicitudEstablecimientoRepository
                .existsByUsuario_IdAndEstadoActual_EstadoSolicitudEstablecimiento_IdAndCuit(
                usuarioSolicitante.getId(),
                estadoPendiente.getId(),
                solicitudEstablecimientoCreateReq.getCuit()
        )) {
            throw new AppException(SolicitudEstablecimientoError.SOLICITUD_ESTABLECIMIENTO_ALREADY_EXISTS);
        }

        SolicitudEstablecimiento nuevaSolicitudEstablecimiento = this.solicitudEstablecimientoMapper
                .solicitudEstablecimientoDtoToSolicitudEstablecimiento(solicitudEstablecimientoCreateReq);

        // 2. Colocar solicitud en estado pendiente
        SolicitudEstablecimientoEstado solicitudEstablecimientoEstadoActual = new SolicitudEstablecimientoEstado();
        solicitudEstablecimientoEstadoActual.setFechaHoraRevision(LocalDateTime.now());
        solicitudEstablecimientoEstadoActual.setRazonRevision("Creacion de solicitud");
        solicitudEstablecimientoEstadoActual.setEstadoSolicitudEstablecimiento(estadoPendiente);

        nuevaSolicitudEstablecimiento.getEstados().add(solicitudEstablecimientoEstadoActual);
        nuevaSolicitudEstablecimiento.setEstadoActual(solicitudEstablecimientoEstadoActual);

        // 3. Obtener y asociar el departamento de la solicitud
        Departamento departamento = this.departamentoRepository
                .findByNombre(solicitudEstablecimientoCreateReq.getDepartamento())
                .orElseThrow(() -> new DepartamentoNotFoundException("No se pudo encontrar el departamento " + solicitudEstablecimientoCreateReq.getDepartamento()));

        nuevaSolicitudEstablecimiento.setDepartamento(departamento);

        // 4. Asociar el usuario de la solicitud
        nuevaSolicitudEstablecimiento.setUsuario(usuarioSolicitante);

        // 5. Obtener los archivos
        List<ArchivoUploadResponse> archivoUploadResponses = this.archivoService
                .getSignedArchivos(
                        solicitudEstablecimientoCreateReq.getArchivos(),
                        this.EXTENSIONES_VALIDAS);

        List<Archivo> archivos = this.archivoMapper.archivoUploadResponseListToArchivoList(archivoUploadResponses);
        nuevaSolicitudEstablecimiento.setPruebas(archivos);

        // 6. Guardar la solicitud en estado pendiente asociado al usuario
        nuevaSolicitudEstablecimiento.setFechaHoraAlta(LocalDateTime.now());
        nuevaSolicitudEstablecimiento = this.solicitudEstablecimientoRepository.save(nuevaSolicitudEstablecimiento);

        // 6. Generar listado de urls para los archivos adjuntos
        SolicitudEstablecimientoCreateResp solicitudEstablecimientoCreateResp = new SolicitudEstablecimientoCreateResp();
        solicitudEstablecimientoCreateResp.setSolicitudId(nuevaSolicitudEstablecimiento.getId().toString());
        solicitudEstablecimientoCreateResp.setNombreEstablecimiento(nuevaSolicitudEstablecimiento.getRazonSocial());
        solicitudEstablecimientoCreateResp.setArchivoUploadResponses(archivoUploadResponses);

        return solicitudEstablecimientoCreateResp;
    }

    public List<SolicitudEstablecimientoShortDTO> obtenerSolicitudesPorUsuario(String emailUsuario) {
        Usuario usuario = this.usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("No se pudo encontrar el usuario"));

        List<SolicitudEstablecimiento> solicitudEstablecimientos = this.solicitudEstablecimientoRepository.findAllByUsuario(usuario);

        return this.solicitudEstablecimientoMapper
                .solicitudEstablecimientosToSolicitudEstablecimientoShortDTOs(solicitudEstablecimientos);
    }
}
