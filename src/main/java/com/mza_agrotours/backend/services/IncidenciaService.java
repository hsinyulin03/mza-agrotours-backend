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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class IncidenciaService {
    private final IncidenciaRepository incidenciaRepository;
    private final IncidenciaMapper incidenciaMapper;
    private final UsuarioRepository usuarioRepository;
    private final EstadoIncidenciaRepository estadoIncidenciaRepository;

    public List<DTOListadoIncidenciaVisitanteResponse> listarIncidenciasDeVisitante(String emailUsuario) {
        return incidenciaMapper.toDTOList(
                incidenciaRepository.findByUsuarioEmailOrderByFechaHoraInicioAsc(emailUsuario)
        );
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
        String busqueda = null;
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
}
