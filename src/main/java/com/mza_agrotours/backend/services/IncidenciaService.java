package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.incidencia.*;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.incidencia.EstadoIncidencia;
import com.mza_agrotours.backend.entities.incidencia.Incidencia;
import com.mza_agrotours.backend.entities.incidencia.IncidenciaEstado;
import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import com.mza_agrotours.backend.mappers.IncidenciaMapper;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.repositories.IncidenciaRepository;
import com.mza_agrotours.backend.repositories.EstadoIncidenciaRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class IncidenciaService {
    private final IncidenciaRepository incidenciaRepository;
    private final IncidenciaMapper incidenciaMapper;
    private final UsuarioRepository usuarioRepository;
    private final EstadoIncidenciaRepository estadoIncidenciaRepository;

    public Page<DTOListadoIncidenciaVisitante> listarIncidenciasDeVisitante(String emailUsuario, Pageable pageable) {
        return incidenciaRepository
                .findByUsuarioEmail(emailUsuario, pageable)
                .map(incidenciaMapper::toDTO);
    }
    public IncidenciasMetricaResponse obtenerFiltroEstados() {
        List<Object[]> resultados = incidenciaRepository.contarPorEstado();

        Map<String, Long> conteos = new HashMap<>();
        long total = 0;

        for (Object[] fila : resultados) {
            EstadoIncidenciaNombre estado = (EstadoIncidenciaNombre) fila[0];
            Long cantidad = (Long) fila[1];
            conteos.put(estado.name(), cantidad);
            total += cantidad;
        }
        // abierta son las que estan en estado reportado y las en revision
        long abiertas = conteos.getOrDefault(EstadoIncidenciaNombre.REPORTADA.name(), 0L) +
                conteos.getOrDefault(EstadoIncidenciaNombre.EN_REVISION.name(), 0L);
        IncidenciasMetricaResponse dto = new IncidenciasMetricaResponse();
        dto.setTotalTodas(total);
        dto.setConteosPorEstado(conteos);
        dto.setTotalAbiertas(abiertas);
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<DTOIncidenciaGestionListado> obtenerIncidencias(DTOIncidenciaFiltro filtro, Pageable pageable) {
        String busqueda = "";
        EstadoIncidenciaNombre estado = null;

        if (filtro != null) {
            if (filtro.getBusqueda() != null && !filtro.getBusqueda().isBlank()) {
                busqueda = filtro.getBusqueda().trim();
            }

            if (filtro.getEstado() != null && !filtro.getEstado().isBlank()
                    && !filtro.getEstado().equalsIgnoreCase("TODAS")) {
                try {
                    estado = EstadoIncidenciaNombre.valueOf(filtro.getEstado().trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new ValidacionNegocioException("El estado de incidencia '" + filtro.getEstado() + "' no es válido");
                }
            }
        }

        return incidenciaRepository.obtenerIncidenciasGestion(busqueda, estado, pageable);
    }

    public DTOFormGestionarIncidencia obtenerformularioGestionarIncidencia(UUID incidenciaId) {
        Incidencia incidencia = obtenerIncidencia(incidenciaId);
        DTOFormGestionarIncidencia dto = new DTOFormGestionarIncidencia();
        dto.setId(String.valueOf(incidencia.getId()));
        dto.setTitulo(incidencia.getTitulo());
        dto.setDescripcion(incidencia.getDescripcion());
        dto.setFechaHoraFin(incidencia.getFechaHoraFin());
        dto.setFechaHoraIncio(incidencia.getFechaHoraInicio());
        dto.setEstadoactual(incidencia.getEstadoActual().getEstado().getNombre());
        EstadoIncidenciaNombre estadoActual = incidencia.getEstadoActual().getEstado().getNombre();
        dto.setEstadoactual(estadoActual);
        List<EstadoIncidenciaNombre> estadosPosibles;
        if (esEstadoFinal(estadoActual)) {
            estadosPosibles = List.of(); // ya está cerrada, no hay más transiciones válidas
        } else {
            estadosPosibles = Arrays.stream(EstadoIncidenciaNombre.values())
                    .filter(estado -> obtenerOrden(estado) > obtenerOrden(estadoActual))
                    .toList();
        }
        dto.setEstadosPosibles(estadosPosibles);
        dto.setTodosLosEstados(List.of(EstadoIncidenciaNombre.values()));

        return dto;
    }
    // EDITAR INCIDENCIA (cambio de estado, panel administrador)
    @Transactional
    public DTOGestionIncidenciaResponse gestionarIncidencia(UUID incidenciaId, DTOGestionIncidenciaRequest dto) {
        Incidencia incidencia = obtenerIncidencia(incidenciaId);
        EstadoIncidencia estadonuevo = obtenerEstadoIncidencia(dto.getEstado());

        EstadoIncidenciaNombre estadoActual = incidencia.getEstadoActual().getEstado().getNombre();
        EstadoIncidenciaNombre estadoNuevo = dto.getEstado();

        DTOGestionIncidenciaResponse response = new DTOGestionIncidenciaResponse();

        if (estadoNuevo == estadoActual) {
            response.setId(incidenciaId);
            response.setMensaje("No se realizó ningún cambio en el estado de la incidencia.");
            return response; // sin cambios
        }

        validarTransicionValida(estadoActual, estadoNuevo);

        if (requiereMotivo(estadoNuevo)) {
            validarMotivoInformado(dto.getMotivo());
        } else{
            dto.setMotivo("Se cambio de estado " + estadoNuevo);
        }

        LocalDateTime ahora = LocalDateTime.now();
        IncidenciaEstado estadoactual = incidencia.getEstadoActual();
        estadoactual.setFechaHoraFin(ahora);

        IncidenciaEstado nuevoEstadoHistorial = new IncidenciaEstado();
        nuevoEstadoHistorial.setFechaHoraIncio(ahora);
        nuevoEstadoHistorial.setEstado(estadonuevo);
        nuevoEstadoHistorial.setFechaHoraFin(null);
        nuevoEstadoHistorial.setMotivo(dto.getMotivo());


        incidencia.getEstados().add(nuevoEstadoHistorial);
        incidencia.setEstadoActual(nuevoEstadoHistorial);

        if (esEstadoFinal(estadoNuevo)) {
            incidencia.setFechaHoraFin(ahora);
        }
        Incidencia incidenciaGuardada = incidenciaRepository.save(incidencia);
        response.setId(incidenciaId);
        response.setMensaje("Incidencia  actualizada a " + estadoNuevo + " exitosamente");

        return response;
    }


    @Transactional
    public IncidenciaCreateResponse crearIncidencia(String emailUsuario, IncidenciaCreateRequest request) {
        Usuario usuario = usuarioRepository.findActiveByEmail(emailUsuario)
                .orElseThrow(() -> new UsuarioNotFound("No se pudo encontrar el usuario " + emailUsuario));

        EstadoIncidencia estadoReportada = estadoIncidenciaRepository.findByNombre(EstadoIncidenciaNombre.REPORTADA)
                .orElseThrow(() -> new EntityNotFoundException("No se pudo encontrar el estado Reportada"));

        LocalDateTime ahora = LocalDateTime.now();

        IncidenciaEstado incidenciaEstado = new IncidenciaEstado();
        incidenciaEstado.setFechaHoraIncio(ahora);
        incidenciaEstado.setFechaHoraFin(null);
        incidenciaEstado.setMotivo("Creación de incidencia");
        incidenciaEstado.setEstado(estadoReportada);

        Incidencia incidencia = new Incidencia();
        incidencia.setTitulo(request.getTitulo());
        incidencia.setDescripcion(request.getDescripcion());
        incidencia.setFechaHoraInicio(ahora);
        incidencia.setFechaHoraFin(null);
        incidencia.setUsuario(usuario);
        incidencia.getEstados().add(incidenciaEstado);
        incidencia.setEstadoActual(incidenciaEstado);

        Incidencia guardada = incidenciaRepository.save(incidencia);
        return IncidenciaCreateResponse.builder()
                .id(guardada.getId())
                .mensaje("Incidencia creada exitosamente")
                .build();
    }
    // Helper methods
    private void validarTransicionValida(EstadoIncidenciaNombre actual, EstadoIncidenciaNombre nuevo) {
        int ordenActual = obtenerOrden(actual);
        int ordenNuevo = obtenerOrden(nuevo);

        if (ordenNuevo < ordenActual) {
            throw new ValidacionNegocioException(
                    "No es posible volver de '" + actual + "' a un estado anterior ('" + nuevo + "')"
            );
        }

        if (esEstadoFinal(actual) && ordenNuevo == ordenActual && nuevo != actual) {
            throw new ValidacionNegocioException(
                    "La incidencia ya se encuentra en un estado final ('" + actual + "') y no puede cambiar a otro estado final distinto"
            );
        }
    }

    private int obtenerOrden(EstadoIncidenciaNombre estado) {
        return switch (estado) {
            case REPORTADA -> 0;
            case EN_REVISION -> 1;
            case RESUELTA, DESESTIMADA -> 2;
        };
    }

    private boolean esEstadoFinal(EstadoIncidenciaNombre estado) {
        return estado == EstadoIncidenciaNombre.RESUELTA || estado == EstadoIncidenciaNombre.DESESTIMADA;
    }

    private boolean requiereMotivo(EstadoIncidenciaNombre estado) {
        return esEstadoFinal(estado);
    }

    private void validarMotivoInformado(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new ValidacionNegocioException("El motivo es obligatorio para este cambio de estado");
        }
    }

    private Incidencia obtenerIncidencia(UUID id) {
        return incidenciaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra la incidencia indicada"));
    }

    private EstadoIncidencia obtenerEstadoIncidencia(EstadoIncidenciaNombre nombre) {
        return estadoIncidenciaRepository.findByNombre(nombre)
                .orElseThrow(() -> new ValidacionNegocioException("No se encuentra configurado el estado " + nombre));
    }

}
