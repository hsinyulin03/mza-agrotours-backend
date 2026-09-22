package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.incidencia.DTOListadoIncidenciaVisitanteResponse;
import com.mza_agrotours.backend.mappers.IncidenciaMapper;
import com.mza_agrotours.backend.repositories.IncidenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaService {
    @Autowired
    private final IncidenciaRepository incidenciaRepository;
    @Autowired
    private final IncidenciaMapper incidenciaMapper;

    public List<DTOListadoIncidenciaVisitanteResponse> listarIncidenciasDeVisitante(String emailUsuario) {
        return incidenciaMapper.toDTOList(
                incidenciaRepository.findByUsuarioEmailOrderByFechaHoraIncioAsc(emailUsuario)
        );
    }
}
