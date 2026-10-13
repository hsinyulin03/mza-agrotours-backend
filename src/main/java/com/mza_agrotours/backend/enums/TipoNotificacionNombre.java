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
            "Recibimos tu solicitud para el establecimineto %s. Te avisaremos cuando la revisemos.",
            EnumSet.of(CanalNotificacion.PUSH)),
    SOLICITUD_ESTABLECIMIENTO_RECHAZADA(
            ScopeNotificacionNombre.VISITANTE,
            "Tu solicitud no fue aprobada",
            "Revisamos tu solicitud para el establecimiento %s: %s.",
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
            "Ya formás parte del equipo del establecimiento %s.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_INICIADA(
            ScopeNotificacionNombre.VISITANTE,
            "Completá el pago de tu reserva",
            "Iniciaste tu reserva para la actividad %s del %s. Tenés hasta el %s para completar el pago, después la reserva se cancela.",
            EnumSet.of(CanalNotificacion.PUSH)),
    RESERVA_PAGADA(
            ScopeNotificacionNombre.VISITANTE,
            "Tu reserva está confirmada",
            "Confirmamos tu reserva para la actividad %s del %s: recibimos tu pago de $%s. ¡Te esperamos!",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_EXPIRADA(
            ScopeNotificacionNombre.VISITANTE,
            "Tu reserva expiró",
            "No recibimos a tiempo el pago de tu reserva para la actividad %s del %s, así que la cancelamos. Podés volver a reservar cuando quieras.",
            EnumSet.of(CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_SIN_REEMBOLSO(
            ScopeNotificacionNombre.VISITANTE,
            "Cancelaste tu reserva",
            "Cancelamos tu reserva para la actividad %s del %s. Como la cancelación fue con %s días o menos de anticipación, no corresponde reembolso.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_REEMBOLSO_EN_PROCESO(
            ScopeNotificacionNombre.VISITANTE,
            "Cancelaste tu reserva",
            "Cancelamos tu reserva para la actividad %s del %s y pedimos el reembolso de $%s. Te avisaremos cuando se realice.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_CON_REEMBOLSO(
            ScopeNotificacionNombre.VISITANTE,
            "Cancelaste tu reserva",
            "Cancelamos tu reserva para la actividad %s del %s y te reembolsamos $%s.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    REEMBOLSO_REALIZADO(
            ScopeNotificacionNombre.VISITANTE,
            "Reembolso realizado",
            "Realizamos el reembolso de tu reserva para la actividad %s del %s por $%s. Según tu medio de pago, puede demorar unos días en verse reflejado.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    REEMBOLSO_NO_AUTOMATICO(
            ScopeNotificacionNombre.VISITANTE,
            "Tu reembolso se gestionará de forma manual",
            "No pudimos realizar automáticamente el reembolso de tu reserva para la actividad %s del %s por $%s. Lo vamos a gestionar de forma manual.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_POR_BAJA_ACTIVIDAD(
            ScopeNotificacionNombre.VISITANTE,
            "Tu reserva fue cancelada",
            "La actividad %s fue dada de baja por el productor, así que cancelamos tu reserva del %s. No se te realizó ningún cobro.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_REEMBOLSADA_PAGO_NO_VALIDO(
            ScopeNotificacionNombre.VISITANTE,
            "No pudimos validar tu pago",
            "El pago de tu reserva para la actividad %s del %s no coincidió con la reserva, así que la cancelamos y pedimos el reembolso de $%s. Podés volver a reservar cuando quieras.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    REEMBOLSO_A_CARGO_AGROTOURS(
            ScopeNotificacionNombre.VISITANTE,
            "Agrotours se encarga de tu reembolso",
            "El establecimiento no realizó a tiempo el reembolso de tu reserva para la actividad %s del %s por $%s, así que lo vamos a realizar nosotros. Te avisaremos cuando esté hecho.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RECORDATORIO_RESERVA(
            ScopeNotificacionNombre.VISITANTE,
            "Recordatorio de tu reserva",
            "Te recordamos que el %s a las %s tenés tu reserva para la actividad %s en el establecimiento %s. ¡Te esperamos!",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    VALORAR_ACTIVIDAD(
            ScopeNotificacionNombre.VISITANTE,
            "¿Cómo te fue en la actividad de %s?",
            "Esperamos que hayas disfrutado en la actividad %s en %s el %s. Contanos tu experiencia calificando la actividad, tu opinión ayuda a otros visitantes.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    RESERVA_CONFIRMADA_PRODUCTOR(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Nueva reserva",
            "Se confirmó una reserva para la actividad %s del %s, para %s persona(s).",
            EnumSet.of(CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_PRODUCTOR(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Se canceló una reserva",
            "El visitante canceló su reserva para la actividad %s del %s, para %s persona(s), y se liberaron sus lugares. No corresponde reembolso.",
            EnumSet.of(CanalNotificacion.PUSH)),
    RESERVA_CANCELADA_REEMBOLSO_PRODUCTOR(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Se canceló una reserva",
            "El visitante canceló su reserva para la actividad %s del %s, para %s persona(s), y se liberaron sus lugares. Se le reembolsan $%s.",
            EnumSet.of(CanalNotificacion.PUSH)),
    REEMBOLSO_REALIZADO_PRODUCTOR(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Reembolso realizado",
            "Se realizó el reembolso de una reserva para la actividad %s del %s por $%s.",
            EnumSet.of(CanalNotificacion.PUSH)),
    REEMBOLSO_PENDIENTE_PRODUCTOR(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Tenés un reembolso pendiente",
            "No se pudo realizar automáticamente el reembolso de una reserva para la actividad %s del %s por $%s. Tenés que realizarlo desde la sección de reembolsos pendientes.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    REEMBOLSO_IMPAGO_PRODUCTOR(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Venció el plazo de un reembolso",
            "No se realizó a tiempo el reembolso de una reserva para la actividad %s del %s por $%s. Lo va a realizar Agrotours y quedará como deuda del establecimiento.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    REEMBOLSO_SISTEMA_PRODUCTOR(
            ScopeNotificacionNombre.ESTABLECIMIENTO,
            "Agrotours realizó un reembolso",
            "Agrotours realizó el reembolso vencido de una reserva para la actividad %s del %s por $%s.",
            EnumSet.of(CanalNotificacion.EMAIL, CanalNotificacion.PUSH)),
    REEMBOLSO_IMPAGO_ADMIN(
            ScopeNotificacionNombre.ADMINISTRADOR,
            "Nuevo reembolso impago",
            "El establecimiento %s no realizó a tiempo el reembolso de $%s de una reserva para la actividad %s del %s. Hay que realizarlo y registrarlo desde el sistema.",
            EnumSet.of(CanalNotificacion.PUSH));

        private final ScopeNotificacionNombre scope;
        private final String titulo;
        private final String plantillaMensaje;   // el %s se rellena al crear la notificación
        private final Set<CanalNotificacion> canales;

}
