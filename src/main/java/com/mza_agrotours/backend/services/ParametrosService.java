package com.mza_agrotours.backend.services;

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

    @Transactional
    public synchronized Parametros getInstance() {
        if (instance == null) {
            instance = parametrosRepository.findFirstByOrderByIdAsc()
                    .orElseGet(() -> parametrosRepository.save(new Parametros()));
        }
        return instance;
    }
}