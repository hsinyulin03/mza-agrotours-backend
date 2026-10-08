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
    private final String urlFront;

    public EmailNotificacionSender(JavaMailSender mailSender,
                            @Value("${notificaciones.email.remitente}") String remitente,
                            @Value("${app.front.url}") String urlFront) {
        this.mailSender = mailSender;
        this.remitente = remitente;
        this.urlFront = urlFront;
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
        mensaje.setText(armarCuerpo(notificacion));

        this.mailSender.send(mensaje);

        log.info("MAIL enviado a {} | asunto: {}| mensaje: {}", destinatario, notificacion.getTitulo(), mensaje.getText());
    }

    // El urlLink es una ruta relativa del front; en el mail se arma la URL completa para que se pueda clickear
    private String armarCuerpo(Notificacion notificacion) {
        String link = notificacion.getUrlLink();
        if (link == null || link.isBlank()) {
            return notificacion.getMensaje();
        }
        return notificacion.getMensaje() + "\n\nPara más detalles, ingrese acá: " + this.urlFront + link;
    }
}
