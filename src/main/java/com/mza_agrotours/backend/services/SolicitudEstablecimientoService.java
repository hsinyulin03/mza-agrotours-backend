package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.SolicitudEstablecimientoCreateReq;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.EstadoSolicitudEstablecimientoNombre;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimiento;
import com.mza_agrotours.backend.entities.solicitud_establecimiento.SolicitudEstablecimientoEstado;
import com.mza_agrotours.backend.exceptions.DepartamentoNotFoundException;
import com.mza_agrotours.backend.exceptions.EstadoSolicitudEstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.mappers.SolicitudEstablecimientoMapper;
import com.mza_agrotours.backend.repositories.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SolicitudEstablecimientoService {
    private final SolicitudEstablecimientoRepository solicitudEstablecimientoRepository;
    private final SolicitudEstablecimientoMapper solicitudEstablecimientoMapper;
    private final EstadoSolicitudEstablecimientoRepository estadoSolicitudEstablecimientoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstablecimientoRepository establecimientoRepository;

    public SolicitudEstablecimientoService(SolicitudEstablecimientoRepository solicitudEstablecimientoRepository,
                                           SolicitudEstablecimientoMapper solicitudEstablecimientoMapper,
                                           EstadoSolicitudEstablecimientoRepository estadoSolicitudEstablecimientoRepository,
                                           DepartamentoRepository departamentoRepository,
                                           UsuarioRepository usuarioRepository,
                                           EstablecimientoRepository establecimientoRepository) {
        this.solicitudEstablecimientoRepository = solicitudEstablecimientoRepository;
        this.solicitudEstablecimientoMapper = solicitudEstablecimientoMapper;
        this.estadoSolicitudEstablecimientoRepository = estadoSolicitudEstablecimientoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.establecimientoRepository = establecimientoRepository;
    }

    public SolicitudEstablecimiento crearSolicitudEstablecimiento(
            SolicitudEstablecimientoCreateReq solicitudEstablecimientoCreateReq,
            String emailUsuario)
            throws Exception {
        // -1. Encontrar si existen establecimientos vigentes con la razón social de la solicitud
        if (establecimientoRepository.existsByRazonSocialIgnoreCaseAndFechaHoraBajaIsNull(solicitudEstablecimientoCreateReq.getRazonSocial())) {
            throw new Exception("Ya existe un establecimiento vigente con esa razón social");
        }

        // 1. Encontrar si el usuario solicitante ya tiene solicitudes pendientes para este establecimiento (por razón social)
        Usuario usuarioSolicitante = this.usuarioRepository.findActiveByEmail(emailUsuario).orElseThrow(() -> new UsuarioNotFound("No se pudo encontrar el usuario " + emailUsuario));

        EstadoSolicitudEstablecimiento estadoPendiente = this.estadoSolicitudEstablecimientoRepository
                .findByNombre(EstadoSolicitudEstablecimientoNombre.PENDIENTE)
                .orElseThrow(() -> new EstadoSolicitudEstablecimientoNotFoundException("No se pudo encontrar el estado pendiente"));

        if (this.solicitudEstablecimientoRepository
                .existsByUsuario_IdAndEstadoActual_EstadoSolicitudEstablecimiento_IdAndRazonSocial(
                usuarioSolicitante.getId(),
                estadoPendiente.getId(),
                solicitudEstablecimientoCreateReq.getRazonSocial()
        )) {
            throw new IllegalStateException("El usuario ya tiene una solicitud pendiente para este establecimiento");
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

        // 5. Guardar la solicitud en estado pendiente asociado al usuario
        return this.solicitudEstablecimientoRepository.save(nuevaSolicitudEstablecimiento);
    }
}
