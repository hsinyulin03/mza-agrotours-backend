package com.mza_agrotours.backend.services.notificaciones;

import com.google.firebase.messaging.*;
import com.mza_agrotours.backend.entities.notificacion.*;
import com.mza_agrotours.backend.enums.CanalNotificacion;
import com.mza_agrotours.backend.enums.EstadoNotificacionTokenNombre;
import com.mza_agrotours.backend.repositories.EstadoNotificacionTokenRepository;
import com.mza_agrotours.backend.repositories.NotificacionTokenRepository;
import com.mza_agrotours.backend.repositories.TokenNotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PushNotificacionSender implements CanalNotificacionSender{

    // TokenNotificacion: el dispositivo (el token de FCM)
    // NotificacionToken: el envío de una notificación a un dispositivo

    private static final Logger log = LoggerFactory.getLogger(PushNotificacionSender.class);

    private final TokenNotificacionRepository tokenNotificacionRepository;
    private final NotificacionTokenRepository notificacionTokenRepository;
    private final EstadoNotificacionTokenRepository estadoNotificacionTokenRepository;

    PushNotificacionSender(TokenNotificacionRepository tokenNotificacionRepository,
                           NotificacionTokenRepository notificacionTokenRepository,
                           EstadoNotificacionTokenRepository estadoNotificacionTokenRepository) {
        this.tokenNotificacionRepository = tokenNotificacionRepository;
        this.notificacionTokenRepository = notificacionTokenRepository;
        this.estadoNotificacionTokenRepository = estadoNotificacionTokenRepository;
    }

    @Override
    public CanalNotificacion getCanal() {
        return CanalNotificacion.PUSH;
    }

    @Override
    public void enviar(Notificacion notificacion) {
        List<TokenNotificacion> tokens = this.tokenNotificacionRepository
                .findByUsuarioAndFechaHoraBajaIsNull(notificacion.getDestinatario());

        // Sin dispositivos registrados no se crea ningun NotificacionToken.
        if (tokens.isEmpty()) {
            return;
        }

        EstadoNotificacionToken pendiente = obtenerEstado(EstadoNotificacionTokenNombre.PENDIENTE);
        EstadoNotificacionToken error = obtenerEstado(EstadoNotificacionTokenNombre.ERROR);

        LocalDateTime ahora = LocalDateTime.now();

        // 1. Un registro PENDIENTE por dispositivo, ANTES de intentar el envio
        List<NotificacionToken> envios = new ArrayList<>();
        for (TokenNotificacion token : tokens) {
            NotificacionToken envio = new NotificacionToken();
            envio.setNotificacion(notificacion);
            envio.setTokenNotificacion(token);
            envio.setEstadoNotificacion(pendiente);
            envio.setFechaHoraAlta(ahora);
            envios.add(envio);
        }
        this.notificacionTokenRepository.saveAll(envios);

        // 2. Un solo llamado a FCM con todos los tokens.
        MulticastMessage mensaje = MulticastMessage.builder()
                .addAllTokens(tokens.stream().map(TokenNotificacion::getToken).toList())
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(notificacion.getTitulo())
                        .setBody(notificacion.getMensaje())
                        .build())
                // data: lo lee el front para saber a donde navegar al tocarla
                .putData("notificacionId", notificacion.getId().toString())
                .putData("urlLink", notificacion.getUrlLink() == null ? "" : notificacion.getUrlLink())
                .build();

        BatchResponse respuesta;
        try {
            respuesta = FirebaseMessaging.getInstance().sendEachForMulticast(mensaje);
        } catch (FirebaseMessagingException e) {
            envios.forEach(envio -> envio.setEstadoNotificacion(error));
            this.notificacionTokenRepository.saveAll(envios);
            throw new IllegalStateException("Fallo el envio push", e);
        }

        registrarResultados(tokens, envios, respuesta, ahora);
        this.notificacionTokenRepository.saveAll(envios);
        this.tokenNotificacionRepository.saveAll(tokens);
    }
    private void registrarResultados(List<TokenNotificacion> tokens,
                                     List<NotificacionToken> envios,
                                     BatchResponse respuesta,
                                     LocalDateTime ahora) {

        EstadoNotificacionToken enviada = obtenerEstado(EstadoNotificacionTokenNombre.ENVIADA);
        EstadoNotificacionToken error = obtenerEstado(EstadoNotificacionTokenNombre.ERROR);

        List<SendResponse> respuestas = respuesta.getResponses();

        for (int i = 0; i < respuestas.size(); i++) {
            SendResponse r = respuestas.get(i);
            NotificacionToken envio = envios.get(i);
            TokenNotificacion token = tokens.get(i);

            if (r.isSuccessful()) {
                envio.setEstadoNotificacion(enviada);
                token.setFechaHoraUltimoUso(ahora);
                continue;
            }

            envio.setEstadoNotificacion(error);
            MessagingErrorCode codigo = r.getException().getMessagingErrorCode();

            // El dispositivo desinstalo la app o el token vencio: se da de baja
            // para no seguir intentando eternamente.
            if (codigo == MessagingErrorCode.UNREGISTERED || codigo == MessagingErrorCode.INVALID_ARGUMENT) {
                token.setFechaHoraBaja(ahora);
            }
        }
    }

    private EstadoNotificacionToken obtenerEstado(EstadoNotificacionTokenNombre nombre) {
        return this.estadoNotificacionTokenRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException(
                        "Falta el estado " + nombre +  "no está registrado en la Base de Datos " ));
    }
}
