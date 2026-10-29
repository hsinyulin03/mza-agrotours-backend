package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.ParametrosDTO;
import com.mza_agrotours.backend.entities.Parametros;
import com.mza_agrotours.backend.events.ParametrosActualizadosEvent;
import com.mza_agrotours.backend.repositories.ParametrosRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Garantiza que exista una única fila de {@link Parametros} en la base de datos.
 * Al ser un bean de Spring administrado como singleton, esta clase es el único punto de acceso
 * a la instancia de parámetros globales de la aplicación.
 */
@Service
public class ParametrosService {
    private final ParametrosRepository parametrosRepository;
    private final ApplicationEventPublisher publisher;

    private volatile Parametros instance;

    public ParametrosService(ParametrosRepository parametrosRepository,
                             ApplicationEventPublisher publisher) {
        this.parametrosRepository = parametrosRepository;
        this.publisher = publisher;
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
    public ParametrosDTO setParametros(ParametrosDTO dto){

        // Se modifica la fila leída de la bd y no la instancia cacheada: la cache se refresca
        // recién después del commit, en refrescarInstancia. Si no existe se crea,
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

        Parametros guardada = parametrosRepository.save(instancia);
        publisher.publishEvent(new ParametrosActualizadosEvent());

        return ParametrosDTO.from(guardada);
    }

    /**
     * Se relee la fila en vez de cachear la guardada para que, si dos admins guardan a la vez,
     * la cache termine con lo último commiteado y no con lo último que pasó por setParametros.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public synchronized void refrescarInstancia(ParametrosActualizadosEvent evento) {
        instance = parametrosRepository.findFirstByOrderByIdAsc().orElse(instance);
    }
}