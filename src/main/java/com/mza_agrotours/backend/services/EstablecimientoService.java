package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.establecimiento.DTOEstablecimientoAlta;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.TipoCultivo;
import com.mza_agrotours.backend.entities.establecimiento.EstadoEstablecimiento;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.EstablecimientoEstado;
import com.mza_agrotours.backend.enums.EstadoEstablecimientoNombre;
import com.mza_agrotours.backend.exceptions.BusinessException;
import com.mza_agrotours.backend.exceptions.EntityAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.mappers.EstablecimientoMapper;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import com.mza_agrotours.backend.repositories.EstadoEstablecimientoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoEstadoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.TipoCultivoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
public class EstablecimientoService  {
    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private EstablecimientoEstadoRepository establecimientoEstadoRepository;

    @Autowired
    private EstadoEstablecimientoRepository estadoEstablecimientoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

//    @Autowired
//    private ProductorRepository productorRepository;

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;
    @Autowired
    private EstablecimientoMapper establecimientoMapper;

    @Transactional
    public void altaEstablecimiento(DTOEstablecimientoAlta dto) {
        validarCuitDisponible(dto.getCuit());

        Departamento departamento = obtenerDepartamento(dto.getDepartamentoId());
        List<TipoCultivo> cultivos = obtenerCultivos(dto.getCultivos());

        Establecimiento establecimiento = establecimientoMapper.dtoEstablecimientoAltaToEstablecimiento(dto);
        establecimiento.setDepartamento(departamento);
        establecimiento.setTiposCultivos(cultivos);
        EstablecimientoEstado estadoInicial = crearEstadoInicial();
        establecimiento.getEstados().add(estadoInicial);
        establecimientoRepository.save(establecimiento);
    }

    private void validarCuitDisponible(String cuit) {
        if (cuit != null && establecimientoRepository.existsByCuit(cuit)) {
            throw new EntityAlreadyExistsException("Ya existe un establecimiento registrado con ese CUIT");
        }
    }

    private Departamento obtenerDepartamento(UUID departamentoId) {
        return departamentoRepository.findById(departamentoId)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra el departamento indicado"));
    }

    private List<TipoCultivo> obtenerCultivos(List<UUID> cultivosIds) {
        if (cultivosIds == null || cultivosIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<TipoCultivo> cultivos = tipoCultivoRepository.findAllById(cultivosIds);
        if (cultivos.size() != cultivosIds.size()) {
            throw new EntityNotFoundException("Uno o más tipos de cultivo no existen");
        }

        return cultivos;
    }

    private EstablecimientoEstado crearEstadoInicial() {
        EstadoEstablecimiento estadoActivo = estadoEstablecimientoRepository
                .findByNombreAndFechaBajaIsNull(EstadoEstablecimientoNombre.ACTIVO)
                .orElseThrow(() -> new BusinessException("No se encuentra configurado el estado ACTIVO"));

        EstablecimientoEstado estadoInicial = new EstablecimientoEstado();
        estadoInicial.setFechaInicio(LocalDateTime.now());
        estadoInicial.setFechaFin(null);
        estadoInicial.setMotivo("Alta de establecimiento");
        estadoInicial.setEstadoEstablecimiento(estadoActivo);
        return estadoInicial;
    }
}
