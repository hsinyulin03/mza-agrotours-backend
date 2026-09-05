package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.enums.CanalNotificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificacionSender implements CanalNotificacionSender {
    private static final Logger log = LoggerFactory.getLogger(EmailNotificacionSender.class);

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailNotificacionSender(JavaMailSender mailSender,
                            @Value("${notificaciones.email.remitente}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public CanalNotificacion getCanal() {
        return CanalNotificacion.EMAIL;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        String destinatario = notificacion.getDestinatario().getEmail();

        // Sin email no hay nada que enviar; la notificacion igual queda en la campanita.
        if (destinatario == null || destinatario.isBlank()) {
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(this.remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject(notificacion.getTitulo());
        mensaje.setText(notificacion.getMensaje());

        this.mailSender.send(mensaje);

        log.info("MAIL enviado a {} | asunto: {}| mensaje: {}", destinatario, notificacion.getTitulo(), notificacion.getMensaje());
    }
}
