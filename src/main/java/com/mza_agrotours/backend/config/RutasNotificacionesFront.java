package com.mza_agrotours.backend.config;

import java.util.UUID;

//se agregó esta clase para no harcodear la urlLink de las notificaciones en cada service y
//si en el futuro hay cambios en las rutas es más fácil para modificar solo en este archivo

public class RutasNotificacionesFront {
    private RutasNotificacionesFront() {}

    public static String detalleSolicitudEstablecimiento(UUID solicitudId) {
        return "/mis-solicitudes/" + solicitudId;
    }
    public static String datosEstablecimiento() {
        return "/panel/datos";
    }

    public static String detalleReserva(UUID reservaId){
        return "/mis-reservas/" + reservaId;
    }

    //TODO: Ajustar esta url a la ruta correcta del front
    public static String valorarExperiencia(UUID reservaId){
        return "/calificaciones/reserva/" + reservaId;
    }
    public static String panelProductor(){
        return "/panel/actividades";
    }
}
