package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.enums.CanalNotificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificacionSender implements CanalNotificacionSender {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificacionSender.class);

    @Override
    public CanalNotificacion getCanal() {
        return CanalNotificacion.EMAIL;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        log.info("MAIL a {} | asunto: {} | cuerpo: {}",
                notificacion.getDestinatario().getEmail(),
                notificacion.getTitulo(),
                notificacion.getMensaje());
    }
}
