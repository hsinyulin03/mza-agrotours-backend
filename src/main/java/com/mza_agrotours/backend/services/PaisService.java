package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.PaisGetDTO;
import com.mza_agrotours.backend.entities.cuenta.Pais;
import com.mza_agrotours.backend.mappers.PaisMapper;
import com.mza_agrotours.backend.repositories.PaisRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaisService {
    private final PaisRepository paisRepository;
    private final PaisMapper paisMapper;

    public PaisService(PaisRepository paisRepository, PaisMapper paisMapper) {
        this.paisRepository = paisRepository;
        this.paisMapper = paisMapper;
    }

    public List<PaisGetDTO> getPaisesDTO() {
        List<Pais> paises = this.paisRepository.findAll();
        return this.paisMapper.paisListToPaisGetDTOList(paises);
    }
}
