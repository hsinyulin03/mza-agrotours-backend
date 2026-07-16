package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.rangoEtario.DTORangoEtarioAlta;
import com.mza_agrotours.backend.dtos.rangoEtario.DTORangoEtarioGet;
import com.mza_agrotours.backend.entities.RangoEtario;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.exceptions.rangoEtario.RangoEtarioAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.rangoEtario.RangoEtarioInvalidoException;
import com.mza_agrotours.backend.mappers.RangoEtarioMapper;
import com.mza_agrotours.backend.repositories.rangoEtario.RangoEtarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RangoEtarioService{
    @Autowired
    private RangoEtarioRepository rangoEtarioRepository;

    @Autowired
    private RangoEtarioMapper rangoEtarioMapper;

    @Transactional
    public DTORangoEtarioGet crearRangoEtario(DTORangoEtarioAlta dto) {

        // Validar que no exista otro rango etario con el mismo nombre
        if (rangoEtarioRepository.existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(dto.getNombre())) {
            throw new RangoEtarioAlreadyExistsException("Ya existe un rango etario con este nombre");
        }
        if (dto.getEdadMaxima() <= dto.getEdadMinima()) {
            throw new RangoEtarioInvalidoException("La edad máxima debe ser mayor que la edad mínima ingresada.");
        }

        RangoEtario rangoEtario = new RangoEtario();
        rangoEtario.setNombre(dto.getNombre());
        rangoEtario.setEdadMinima(dto.getEdadMinima());
        rangoEtario.setEdadMaxima(dto.getEdadMaxima());
        rangoEtario.setFechaHoraBaja(null);

        RangoEtario rangoGuardado = rangoEtarioRepository.save(rangoEtario);
        return rangoEtarioMapper.rangoEtariotoDTORangoEtarioGet(rangoGuardado);
    }

    @Transactional(readOnly = true)
    public List<DTORangoEtarioGet> listarRangosActivos() {
        List<RangoEtario> rangosActivos = rangoEtarioRepository.findAllByFechaHoraBajaIsNull();
        return rangoEtarioMapper.rangoEtarioListtoDTORangoEtarioGetList(rangosActivos);
    }

    @Transactional
    public void darDeBaja(UUID id) {
        RangoEtario rango = rangoEtarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rango etario no encontrado"));

        rango.setFechaHoraBaja(LocalDateTime.now());
        rangoEtarioRepository.save(rango);
    }

    //TODO: Falta validaciones de solapamiento y huecos vacíos de edades
}