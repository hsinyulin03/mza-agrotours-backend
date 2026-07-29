package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.DepartamentoDTO;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.mappers.DepartamentoMapper;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartamentoService {
    private final DepartamentoRepository departamentoRepository;
    private final DepartamentoMapper departamentoMapper;

    public DepartamentoService(DepartamentoRepository departamentoRepository, DepartamentoMapper departamentoMapper) {
        this.departamentoRepository = departamentoRepository;
        this.departamentoMapper = departamentoMapper;
    }

    @Transactional
    public List<DepartamentoDTO> getAllDepartamentos() {
        List<Departamento> departamentos = this.departamentoRepository.findAll();
        return this.departamentoMapper.departamentoListToDepartamentoDTOList(departamentos);
    }
}
