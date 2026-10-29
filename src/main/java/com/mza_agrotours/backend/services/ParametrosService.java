package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.ParametrosDTO;
import com.mza_agrotours.backend.entities.Parametros;
import com.mza_agrotours.backend.repositories.ParametrosRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garantiza que exista una única fila de {@link Parametros} en la base de datos.
 * Al ser un bean de Spring administrado como singleton, esta clase es el único punto de acceso
 * a la instancia de parámetros globales de la aplicación.
 */
@Service
public class ParametrosService {
    private final ParametrosRepository parametrosRepository;

    private volatile Parametros instance;

    public ParametrosService(ParametrosRepository parametrosRepository) {
        this.parametrosRepository = parametrosRepository;
    }

    public synchronized Parametros getInstance() {
        if (instance == null) {
            instance = parametrosRepository.findFirstByOrderByIdAsc()
                    .orElseGet(() -> parametrosRepository.save(new Parametros()));
        }
        return instance;
    }

    public ParametrosDTO getParametros() {
        return ParametrosDTO.from(getInstance());
    }

    @Transactional
    public synchronized ParametrosDTO setParametros(ParametrosDTO dto){

        // Se modifica la fila leída de la bd y no la instancia cacheada, para que si falla el
        // guardado la cache no quede con valores que no están en la bd. Si no existe se crea,
        // como el dto trae todos los campos no se pierde nada
        Parametros instancia = parametrosRepository.findFirstByOrderByIdAsc()
                .orElseGet(Parametros::new);
        instancia.setDiasImpagoReserva(dto.diasImpagoReserva());
        instancia.setDiasMaxCrearActividad(dto.diasMaxCrearActividad());
        instancia.setDiasMinReembolso(dto.diasMinReembolso());
        instancia.setPorcentajeComision(dto.porcentajeComision());
        instancia.setTtlReserva(dto.ttlReserva());

        // Parametros para el sistema de calificaciones
        instancia.setCantidadCalificacionesFicticias(dto.cantidadCalifiacionesFicticias());
        instancia.setPuntajeCalificacionFicticia(dto.puntajeCalificacionFicticia());
        instancia.setUsarCalificacionGlobalComoPuntaje(dto.usarCalificacionGlobalComoPuntaje());

        // Guardamos en la bd y actualizamos la instancia en memoria
        instance = parametrosRepository.save(instancia);

        return ParametrosDTO.from(instance);
    }
}