package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum TipoNotificacionNombre {
    SOLICITUD_ESTABLECIMIENTO_CREADA(
            "Solicitud recibida",
            "Recibimos tu solicitud para %s. Te avisaremos cuando la revisemos.",
            EnumSet.of(CanalNotificacion.PUSH)),

    SOLICITUD_ESTABLECIMIENTO_RECHAZADA(
            "Tu solicitud no fue aprobada",
            "Revisamos tu solicitud para %s: %s.",
            EnumSet.of(CanalNotificacion.EMAIL,CanalNotificacion.PUSH)),
    SOLICITUD_ESTABLECIMIENTO_APROBADA(
            "Solicitud aprobada",
            "Tu establecimiento %s ya está habilitado.",
            EnumSet.of(CanalNotificacion.EMAIL,CanalNotificacion.PUSH)),
    PRODUCTOR_AGREGADO(
            "Te sumaron a un establecimiento",
            "Ya formás parte del equipo de %s.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_POR_BAJA_ACTIVIDAD(
            "Tu reserva fue cancelada",
            "La actividad %s fue dada de baja por el productor, así que cancelamos tu reserva del %s. No se te realizó ningún cobro.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH));

        private final String titulo;
        private final String plantillaMensaje;   // el %s se rellena al crear la notificación
        private final Set<CanalNotificacion> canales;

}
