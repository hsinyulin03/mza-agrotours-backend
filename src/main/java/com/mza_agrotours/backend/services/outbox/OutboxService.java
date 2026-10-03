package com.mza_agrotours.backend.services.outbox;

import com.mza_agrotours.backend.config.outbox.OutboxStrategyFactory;
import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.enums.outbox.EstadoOutbox;
import com.mza_agrotours.backend.enums.outbox.TipoOperacion;
import com.mza_agrotours.backend.repositories.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class OutboxService {
    private static final int MAX_REINTENTOS = 4;
    private static Logger logger = LoggerFactory.getLogger(OutboxService.class);
    private final OutboxRepository outboxRepository;
    private final OutboxStrategyFactory outboxStrategyFactory;
    private final OutboxService self;

    private final Random random = new Random();

    public OutboxService(OutboxRepository outboxRepository, OutboxStrategyFactory outboxStrategyFactory, @Lazy OutboxService self) {
        this.outboxRepository = outboxRepository;
        this.outboxStrategyFactory = outboxStrategyFactory;
        this.self = self;
    }

    public void resolverPendientes() {
        List<Outbox> outboxes = this.outboxRepository.findByEstado(EstadoOutbox.PENDIENTE);
        LocalDateTime ahora = LocalDateTime.now();
        for (Outbox outbox : outboxes) {
            if(outbox.getFechaHoraProximoIntento().isAfter(ahora)) {
                continue;
            }

            self.resolverOutbox(outbox);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resolverOutbox(Outbox outbox) {
        try {
            outboxStrategyFactory.getStrategy(outbox).resolver(outbox);
            outbox.setEstado(EstadoOutbox.EXITOSO);
            outboxRepository.save(outbox);
        } catch (Exception e) {
            logger.error("Error al resolver outbox con id {}: {}", outbox.getId(), e.getMessage());
            handleIntentoFallido(outbox);
        }
    }

    @Transactional
    public Outbox crearOutboxPendiente(String entidadId, TipoOperacion tipoOperacion) {
        Outbox outbox = new Outbox();
        outbox.setEntidadId(entidadId);
        outbox.setEstado(EstadoOutbox.PENDIENTE);

        LocalDateTime ahora = LocalDateTime.now();
        outbox.setFechaHoraAlta(ahora);
        outbox.setFechaHoraProximoIntento(ahora.plusSeconds(5));

        outbox.setReintentos(0);
        outbox.setOperacion(tipoOperacion);

        return this.outboxRepository.save(outbox);
    }

    public void handleIntentoFallido(Outbox outbox) {
        if (outbox.getReintentos() >= MAX_REINTENTOS) {
            outbox.setEstado(EstadoOutbox.FALLIDO);
            this.outboxRepository.save(outbox);
            return;
        }

        outbox.setReintentos(outbox.getReintentos() + 1);
        outbox.setFechaHoraProximoIntento(LocalDateTime.now().plusSeconds((int) (Math.pow(2, outbox.getReintentos()) + randomInt(1, 5))));
        this.outboxRepository.save(outbox);
    }

    private int randomInt(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
