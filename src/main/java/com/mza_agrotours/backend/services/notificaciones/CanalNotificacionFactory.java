package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.enums.CanalNotificacion;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CanalNotificacionFactory {
    private final Map<CanalNotificacion, CanalNotificacionSender> senders;

    public CanalNotificacionFactory(List<CanalNotificacionSender> implementaciones) {
        this.senders = implementaciones.stream()
                .collect(Collectors.toMap(CanalNotificacionSender::getCanal, s -> s));
    }
    public CanalNotificacionSender get(CanalNotificacion canal) {
        return this.senders.get(canal);
    }
}
