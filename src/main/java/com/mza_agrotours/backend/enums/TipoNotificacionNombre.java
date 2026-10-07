package com.mza_agrotours.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum TipoNotificacionNombre {
    SOLICITUD_ESTABLECIMIENTO_CREADA(
            ScopeNotificacionNombre.VISITANTE,
            "Solicitud recibida",
            "Recibimos tu solicitud para %s. Te avisaremos cuando la revisemos.",
            EnumSet.of(CanalNotificacion.PUSH)),
    SOLICITUD_ESTABLECIMIENTO_RECHAZADA(
            ScopeNotificacionNombre.VISITANTE,
            "Tu solicitud no fue aprobada",
            "Revisamos tu solicitud para %s: %s.",
            EnumSet.of(CanalNotificacion.EMAIL,CanalNotificacion.PUSH)),
    SOLICITUD_ESTABLECIMIENTO_APROBADA(
            ScopeNotificacionNombre.VISITANTE,
            "Solicitud aprobada",
            "Tu establecimiento %s ya está habilitado.",
            EnumSet.of(CanalNotificacion.EMAIL,CanalNotificacion.PUSH)),
    SOLICITUD_ESTABLECIMIENTO_POR_REVISAR(
            ScopeNotificacionNombre.ADMINISTRADOR,
            "Nueva solicitud de establecimiento",
            "%s solicitó dar de alta el establecimiento %s.",
            EnumSet.of(CanalNotificacion.PUSH)),
    PRODUCTOR_AGREGADO(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Te sumaron a un establecimiento",
            "Ya formás parte del equipo de %s.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_POR_BAJA_ACTIVIDAD(
            ScopeNotificacionNombre.VISITANTE,
            "Tu reserva fue cancelada",
            "La actividad %s fue dada de baja por el productor, así que cancelamos tu reserva del %s. No se te realizó ningún cobro.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH));

        private final ScopeNotificacionNombre scope;
        private final String titulo;
        private final String plantillaMensaje;   // el %s se rellena al crear la notificación
        private final Set<CanalNotificacion> canales;

}
