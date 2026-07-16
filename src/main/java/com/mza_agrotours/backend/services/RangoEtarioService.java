package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.DTORangoEtarioAlta;
import com.mza_agrotours.backend.entities.RangoEtario;
import com.mza_agrotours.backend.repositories.RangoEtarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RangoEtarioService{
    @Autowired
    private RangoEtarioRepository rangoEtarioRepository;

    @Transactional
    public void crearRangoEtario(DTORangoEtarioAlta dto) {

        // Validar que no exista otro rango etario con el mismo nombre
        if (rangoEtarioRepository.existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(dto.getNombre())) {
            throw new RuntimeException("Ya existe un rango etario con este nombre");
        }


        if (dto.getEdadMaxima() <= dto.getEdadMinima()) {
            throw new IllegalArgumentException("La edad máxima debe ser mayor que la edad mínima ingresada.");
        }

        RangoEtario rangoEtario = new RangoEtario();
        rangoEtario.setNombre(dto.getNombre());
        rangoEtario.setEdadMinima(dto.getEdadMinima());
        rangoEtario.setEdadMaxima(dto.getEdadMaxima());
        rangoEtario.setFechaHoraBaja(null);

        rangoEtarioRepository.save(rangoEtario);
    }

    @Transactional
    public void darDeBaja(UUID id) {
        RangoEtario rango = rangoEtarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rango etario no encontrado"));

        rango.setFechaHoraBaja(LocalDateTime.now());
        rangoEtarioRepository.save(rango);
    }

    //TODO: Falta validaciones de solapamiento y huecos vacíos de edades
}