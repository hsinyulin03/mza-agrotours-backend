package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoAM;
import com.mza_agrotours.backend.dtos.tipoCultivo.DTOTipoCultivoDatos;
import com.mza_agrotours.backend.entities.cultivo.Estacionalidad;
import com.mza_agrotours.backend.entities.cultivo.EstacionalidadMes;
import com.mza_agrotours.backend.entities.cultivo.TipoCultivo;
import com.mza_agrotours.backend.enums.EstacionalidadNombre;
import com.mza_agrotours.backend.enums.Mes;
import com.mza_agrotours.backend.exceptions.EntityAlreadyExistsException;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.exceptions.ValidacionNegocioException;
import com.mza_agrotours.backend.repositories.TipoCultivo.EstacionalidadRepository;
import com.mza_agrotours.backend.repositories.TipoCultivoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TipoCultivoService {

    @Autowired
    private TipoCultivoRepository tipoCultivoRepository;

    @Autowired
    private EstacionalidadRepository estacionalidadRepository;

    // ALTA TIPO DE CULTIVO
    @Transactional
    public DTOTipoCultivoDatos altaTipoCultivo(DTOTipoCultivoAM dto) {
        validarNombreDisponible(dto.getNombre());

        TipoCultivo tipoCultivo = new TipoCultivo();
        tipoCultivo.setNombre(dto.getNombre());
        tipoCultivo.setDescripcion(dto.getDescripcion());
        tipoCultivo.setBeneficios(dto.getBeneficios());
        tipoCultivo.setEstacionalidadMeses(construirEstacionalidadMeses(dto.getEstacionalidadPorMes()));

        TipoCultivo guardado = tipoCultivoRepository.save(tipoCultivo);

        return mapearADatos(guardado);
    }
    // OBTENER DATOS TIPO CULTIVO (para prellenar el formulario de editar)
    @Transactional
    public DTOTipoCultivoDatos obtenerDatosTipoCultivo(UUID id) {
        TipoCultivo tipoCultivo = obtenerTipoCultivo(id);
        return mapearADatos(tipoCultivo);
    }
    // MODIFICAR TIPO CULTIVO
    @Transactional
    public DTOTipoCultivoDatos modificarTipoCultivo(UUID id, DTOTipoCultivoAM dto) {
        TipoCultivo tipoCultivo = obtenerTipoCultivo(id);
        validarNombreDisponible(dto.getNombre());

        tipoCultivo.setNombre(dto.getNombre());
        tipoCultivo.setDescripcion(dto.getDescripcion());
        tipoCultivo.setBeneficios(dto.getBeneficios());

        actualizarEstacionalidad(tipoCultivo, dto.getEstacionalidadPorMes());

        TipoCultivo guardado = tipoCultivoRepository.save(tipoCultivo);
        return mapearADatos(guardado);
    }









    private void validarNombreDisponible(String nombre) {
        if (tipoCultivoRepository.existsByNombreIgnoreCaseAndFechaHoraBajaIsNull(nombre)) {
            throw new EntityAlreadyExistsException("Ya existe un tipo de cultivo con este nombre");
        }
    }

    private List<EstacionalidadMes> construirEstacionalidadMeses(List<EstacionalidadNombre> estacionalidadPorMes) {
        // Lista donde se van guardando las EstacionalidadMes creadas
        List<EstacionalidadMes> resultado = new ArrayList<>();
        // Obtiene todos los meses del enum Mes en el orden en que fueron definidos
        Mes[] meses = Mes.values();

        // Valida que la cantidad de estacionalidades recibidas coincida
        // exactamente con la cantidad de meses del año.
        if (estacionalidadPorMes == null || estacionalidadPorMes.size() != meses.length) {
            throw new ValidacionNegocioException(
                    "Se esperaban " + meses.length + " estacionalidades (una por mes), se recibieron " + estacionalidadPorMes.size()
            );
        }
        // Recorre la lista de estacionalidades(reposo-cosecha-crecimiento) por mes recibida
        for (int i = 0; i < estacionalidadPorMes.size(); i++) {
            // Convierte el estado/fase recibido ej COSECHA
            // en la entidad Estacionalidad correspondiente de la base de datos
            Estacionalidad estacionalidad = obtenerEstacionalidadPorNombre(estacionalidadPorMes.get(i));
            // Crea una nueva estacionalidad mes
            EstacionalidadMes em = new EstacionalidadMes();
            //el mes correspondiente según la posición del recorrido ej i=0 ENERO
            em.setMes(meses[i]);
            // asigna la estacionalidad obtenida para ese mes
            em.setEstacionalidad(estacionalidad);
            // agrega la relación a la lista resultado
            resultado.add(em);
        }

        return resultado;
    }

    private Estacionalidad obtenerEstacionalidadPorNombre(EstacionalidadNombre nombre) {
        return estacionalidadRepository.findByNombre(nombre)
                .orElseThrow(() -> new ValidacionNegocioException("No se encuentra configurada la estacionalidad " + nombre));
    }

    private DTOTipoCultivoDatos mapearADatos(TipoCultivo tipoCultivo) {
        DTOTipoCultivoDatos dto = new DTOTipoCultivoDatos();
        dto.setNombre(tipoCultivo.getNombre());
        dto.setDescripcion(tipoCultivo.getDescripcion());
        dto.setBeneficios(tipoCultivo.getBeneficios());
        dto.setEstacionalidadPorMes(obtenerEstacionalidadPorMes(tipoCultivo));
        return dto;
    }
    private List<EstacionalidadNombre> obtenerEstacionalidadPorMes(TipoCultivo tipoCultivo) {
        // Crea un mapa donde la clave es el mes y el valor es el nombre
        // de la estacionalidad correspondiente.
        // ej ENERO -> COSECHA
        Map<Mes, EstacionalidadNombre> porMes = tipoCultivo.getEstacionalidadMeses().stream()
                .collect(Collectors.toMap(
               //  el mes como clave.
                        EstacionalidadMes::getMes,
                        //nombre de la estacionalidad como valor
                        em -> em.getEstacionalidad().getNombre()
                ));
        //Recorre todos los meses del año en el orden definido por el enum
        // y obtiene la estacionalidad asociada a cada uno desde el mapa.
        return Arrays.stream(Mes.values())
                .map(mes -> {
                    EstacionalidadNombre nombre = porMes.get(mes);
                    if (nombre == null) {
                        throw new IllegalStateException(
                                "El tipo de cultivo '" + tipoCultivo.getNombre()
                                        + "' no tiene estacionalidad cargada para el mes " + mes
                        );
                    }
                    return nombre;
                })
                .toList();
    }

    private TipoCultivo obtenerTipoCultivo(UUID id) {
        return tipoCultivoRepository.findByIdAndFechaHoraBajaIsNull(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra el tipo de cultivo indicado"));
    }

    private void actualizarEstacionalidad(TipoCultivo tipoCultivo, List<EstacionalidadNombre> estacionalidadPorMes) {
        //Obtiene la lista de estacionalidades actualmente asociadas al cultivo
        List<EstacionalidadMes> estacionalidadActual = tipoCultivo.getEstacionalidadMeses();
        // Construye una nueva lista de estacionalidadMes a partir de los estados
        // recibidos (uno por cada mes del año).
        List<EstacionalidadMes> estacionalidadNueva = construirEstacionalidadMeses(estacionalidadPorMes);
        // Elimina todas las relaciones de estacionalidad actuales del cultivo.
        estacionalidadActual.clear();
        // Agrega las nuevas relaciones de estacionalidad a la lista
        estacionalidadActual.addAll(estacionalidadNueva);
    }

}