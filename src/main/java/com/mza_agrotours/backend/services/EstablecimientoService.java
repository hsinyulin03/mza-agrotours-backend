package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.establecimiento.DTOEstablecimientoAlta;
import com.mza_agrotours.backend.entities.Departamento;
import com.mza_agrotours.backend.entities.TipoCultivo;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.establecimiento.EstablecimientoEstado;
import com.mza_agrotours.backend.enums.EstadoEstablecimiento;
import com.mza_agrotours.backend.mappers.EstablecimientoMapper;
import com.mza_agrotours.backend.repositories.DepartamentoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoEstadoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.repositories.TipoCultivoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class EstablecimientoService extends BaseEntityServiceImpl<Establecimiento, Long> {
    @Autowired
    private EstablecimientoRepository establecimientoRepository;

    @Autowired
    private EstablecimientoEstadoRepository establecimientoEstadoRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

//    @Autowired
//    private ProductorRepository productorRepository;

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;
    @Autowired
    private EstablecimientoMapper establecimientoMapper;

    @Transactional
    public void altaEstablecimiento(DTOEstablecimientoAlta dto) throws Exception {
            // Validaciones
            if (dto.getCuit() != null && establecimientoRepository.existsByCuit(dto.getCuit())) {
                throw new Exception("Ya existe un establecimiento registrado con ese CUIT");
            }

            // Validaciones entidades relacionadas
            Departamento departamento = departamentoRepository.findById(dto.getDepartamentoId())
                    .orElseThrow(() -> new RuntimeException("No se encuentra el departamento indicado"));

            List<TipoCultivo> cultivos = (dto.getCultivos() == null || dto.getCultivos().isEmpty())
                    ? new ArrayList<>()
                    : tipoCultivoRepository.findAllById(dto.getCultivos());

            if (cultivos.size() != dto.getCultivos().size()) {
                throw new Exception("Uno o más tipos de cultivo no existen");
            }

            // Mapeo
            Establecimiento establecimiento = establecimientoMapper.toEntity(dto);

            // Completar las relaciones
            establecimiento.setDepartamento(departamento);
            establecimiento.setTiposCultivos(cultivos);
            establecimiento = establecimientoRepository.save(establecimiento);
            // Estado inicial
            EstablecimientoEstado estadoInicial = new EstablecimientoEstado();
            estadoInicial.setFechaInicio(LocalDateTime.now());
            estadoInicial.setFechaFin(null);
            estadoInicial.setMotivo("Alta de establecimiento");
            estadoInicial.setEstado(EstadoEstablecimiento.ACTIVO);
            estadoInicial.setEstablecimiento(establecimiento);

            establecimientoEstadoRepository.save(estadoInicial);

            establecimiento.getEstados().add(estadoInicial);

    }
}
