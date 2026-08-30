package com.mza_agrotours.backend.config;

import java.util.UUID;

//se agregó esta clase para no harcodear la urlLink de las notificaciones en cada service y
//si en el futuro hay cambios en las rutas es más fácil para modificar solo en este archivo

public class RutasNotificacionesFront {
    private RutasNotificacionesFront() {}

    public static String solicitudEstablecimiento(UUID solicitudId) {
        return "/solicitudes-establecimiento/me/" + solicitudId;
    }

    public static String establecimiento(UUID establecimientoId) {
        return "/establecimientos/" + establecimientoId;
    }

}
