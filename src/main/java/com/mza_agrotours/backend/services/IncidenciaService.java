package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.incidencia.DTOListadoIncidenciaVisitanteResponse;
import com.mza_agrotours.backend.dtos.incidencia.IncidenciaCreateResponse;
import com.mza_agrotours.backend.dtos.incidencia.IncidenciaCreateRequest;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.incidencia.EstadoIncidencia;
import com.mza_agrotours.backend.entities.incidencia.Incidencia;
import com.mza_agrotours.backend.entities.incidencia.IncidenciaEstado;
import com.mza_agrotours.backend.enums.EstadoIncidenciaNombre;
import com.mza_agrotours.backend.mappers.IncidenciaMapper;
import com.mza_agrotours.backend.exceptions.UsuarioNotFound;
import com.mza_agrotours.backend.repositories.IncidenciaRepository;
import com.mza_agrotours.backend.repositories.EstadoIncidenciaRepository;
import com.mza_agrotours.backend.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaService {
    private final IncidenciaRepository incidenciaRepository;
    private final IncidenciaMapper incidenciaMapper;
    private final UsuarioRepository usuarioRepository;
    private final EstadoIncidenciaRepository estadoIncidenciaRepository;

    public List<DTOListadoIncidenciaVisitanteResponse> listarIncidenciasDeVisitante(String emailUsuario) {
        return incidenciaMapper.toDTOList(
                incidenciaRepository.findByUsuarioEmailOrderByFechaHoraIncioAsc(emailUsuario)
        );
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
        incidencia.setFechaHoraIncio(ahora);
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
