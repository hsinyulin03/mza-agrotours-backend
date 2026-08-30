package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.dtos.notificacion.NotificacionCreadaEvent;
import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.enums.CanalNotificacion;
import com.mza_agrotours.backend.repositories.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificacionDispatcher {
    private static final Logger log = LoggerFactory.getLogger(NotificacionDispatcher.class);

    private final NotificacionRepository notificacionRepository;
    private final CanalNotificacionFactory canalFactory;

    public NotificacionDispatcher(NotificacionRepository notificacionRepository,
                                  CanalNotificacionFactory canalFactory) {
        this.notificacionRepository = notificacionRepository;
        this.canalFactory = canalFactory;
    }
    /**
     * AFTER_COMMIT: cuando esto corre, la operacion de negocio ya esta guardada y es
     * irreversible. Un fallo de envio no puede revertirla, y la notificacion sigue
     * estando en la campanita aunque el canal externo falle.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void despachar(NotificacionCreadaEvent evento) {
        Notificacion notificacion = this.notificacionRepository
                .findById(evento.getNotificacionId())
                .orElse(null);

        if (notificacion == null) {
            return;
        }

        for (CanalNotificacion canal : notificacion.getTipoNotificacion().getNombre().getCanales()) {
            CanalNotificacionSender sender = this.canalFactory.get(canal);
            if (sender == null) {
                continue;
            }

            try {
                sender.enviar(notificacion);
            } catch (Exception e) {
                log.error("Fallo el envio de la notificacion {} por el canal {}",
                        notificacion.getId(), canal, e);
            }
        }
    }
}
