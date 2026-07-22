package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.rangoEtario.DTOListadoRangoEtarioResponse;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        // Validación de solapamiento
        List<RangoEtario> rangosActivos = rangoEtarioRepository.findAllByFechaHoraBajaIsNull();
        for (RangoEtario existente : rangosActivos) {
            boolean haySolapamiento = dto.getEdadMinima() <= existente.getEdadMaxima() &&
                    dto.getEdadMaxima() >= existente.getEdadMinima();
            if (haySolapamiento) {
                throw new RangoEtarioInvalidoException(
                        "No se puede crear: El rango se solapa con '" + existente.getNombre() +
                                "' (" + existente.getEdadMinima() + "-" + existente.getEdadMaxima() + " años)."
                );
            }
        }
        RangoEtario rangoEtario = new RangoEtario();
        rangoEtario.setNombre(dto.getNombre());
        rangoEtario.setEdadMinima(dto.getEdadMinima());
        rangoEtario.setEdadMaxima(dto.getEdadMaxima());
        rangoEtario.setFechaHoraBaja(null);

        RangoEtario rangoGuardado = rangoEtarioRepository.save(rangoEtario);
        return rangoEtarioMapper.rangoEtariotoDTORangoEtarioGet(rangoGuardado);
    }
    //Cuando creo una nueva actividad, en el form se debería mostrar esto
    @Transactional(readOnly = true)
    public List<DTORangoEtarioGet> listarRangosActivos() {
        List<RangoEtario> rangosActivos = rangoEtarioRepository.findAllByFechaHoraBajaIsNull();
        return rangoEtarioMapper.rangoEtarioListtoDTORangoEtarioGetList(rangosActivos);
    }

    //Consultar listado de rango etario, panel admin
    @Transactional(readOnly = true)
    public DTOListadoRangoEtarioResponse obtenerListaRangos() {

        List<RangoEtario> todosLosRangos = rangoEtarioRepository.findAll();

        List<RangoEtario> rangosActivos = todosLosRangos.stream()
                .filter(r -> r.getFechaHoraBaja() == null)
                .collect(Collectors.toList());

        List<DTORangoEtarioGet> rangosActivosDTO = rangoEtarioMapper.rangoEtarioListtoDTORangoEtarioGetList(todosLosRangos);

        List<String> alertasHuecos = calcularHuecos(rangosActivos);

        return new DTOListadoRangoEtarioResponse(rangosActivosDTO, alertasHuecos);
    }

    @Transactional
    public void darDeBaja(UUID id) {
        RangoEtario rango = rangoEtarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rango etario no encontrado"));

        rango.setFechaHoraBaja(LocalDateTime.now());
        rangoEtarioRepository.save(rango);
    }

    public List<String> calcularHuecos(List<RangoEtario> rangosActivos) {
        List<String> huecos = new ArrayList<>();


        if (rangosActivos.isEmpty()) {
            huecos.add("0 a 120 años");
            return huecos;
        }

        // Ordenamos por edad mínima de menor a mayor
        rangosActivos.sort(Comparator.comparingInt(RangoEtario::getEdadMinima));

        // Verificamos el hueco inicial (si no empieza en 0)
        RangoEtario primerRango = rangosActivos.get(0);
        if (primerRango.getEdadMinima() > 0) {
            int finHueco = primerRango.getEdadMinima() - 1;
            huecos.add("0 a " + finHueco + " años");
        }

        // Verificamos los huecos intermedios
        for (int i = 0; i < rangosActivos.size() - 1; i++) {
            int maxActual = rangosActivos.get(i).getEdadMaxima();
            int minSiguiente = rangosActivos.get(i + 1).getEdadMinima();

            // Si hay una diferencia mayor a 1 entre el fin de uno y el inicio del otro
            if (minSiguiente > maxActual + 1) {
                int inicioHueco = maxActual + 1;
                int finHueco = minSiguiente - 1;
                huecos.add(inicioHueco + " a " + finHueco + " años");
            }
        }

        // Verificamos el hueco final (si no llega a 120)
        RangoEtario ultimoRango = rangosActivos.get(rangosActivos.size() - 1);
        //TODO: SETEAR COMO PARÁMETRO GLOBAL
        int EDAD_MAXIMA_SISTEMA = 120; // Según el límite de tu interfaz

        if (ultimoRango.getEdadMaxima() < EDAD_MAXIMA_SISTEMA) {
            int inicioHueco = ultimoRango.getEdadMaxima() + 1;
            huecos.add(inicioHueco + " a " + EDAD_MAXIMA_SISTEMA + " años");
        }

        return huecos;
    }

}