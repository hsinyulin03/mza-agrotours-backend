package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.entities.notificacion.Notificacion;
import com.mza_agrotours.backend.enums.CanalNotificacion;
import org.springframework.stereotype.Service;

@Service
public interface CanalNotificacionSender {
    CanalNotificacion getCanal();
    void enviar(Notificacion notificacion);
}
