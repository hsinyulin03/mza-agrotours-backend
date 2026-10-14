package com.mza_agrotours.backend.services.notificaciones;

import com.mza_agrotours.backend.config.RutasNotificacionesFront;
import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.PermisoCodigo;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Avisos sobre una reserva o su reembolso al visitante, a los productores del establecimiento y a los administradores.
 */
@Component
public class ReservaNotificador {
    public static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificacionService notificacionService;

    public ReservaNotificador(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Notifica al visitante dueño de la reserva, con el link al detalle de la reserva. Los mensajes de los tipos
     * de reserva empiezan con el nombre de la actividad y la fecha y hora del día, que se completan acá.
     *
     * @param reserva reserva sobre la que se avisa
     * @param tipo tipo de notificación
     * @param datos datos del mensaje que siguen a la actividad y la fecha
     */
    public void notificarVisitante(Reserva reserva, TipoNotificacionNombre tipo, Object... datos){
        notificacionService.crearNotificacion(
                reserva.getVisitante().getUsuario(),
                tipo,
                null,
                RutasNotificacionesFront.detalleReserva(reserva.getId()),
                datosMensajeReserva(reserva, datos));
    }

    /**
     * Notifica a los productores del establecimiento de la reserva cuyo rol tiene el permiso indicado. Como en
     * {@link #notificarVisitante}, los mensajes empiezan con el nombre de la actividad y la fecha y hora del día.
     *
     * @param reserva reserva sobre la que se avisa
     * @param permiso permiso que necesita el productor para recibir el aviso
     * @param tipo tipo de notificación, de scope ESTABLECIMIENTO
     * @param enlace link del panel del establecimiento
     * @param datos datos del mensaje que siguen a la actividad y la fecha
     */
    public void notificarProductores(Reserva reserva, PermisoCodigo permiso, TipoNotificacionNombre tipo, String enlace, Object... datos){
        notificacionService.notificarProductores(
                reserva.getActividad().getEstablecimiento(),
                permiso,
                tipo,
                enlace,
                datosMensajeReserva(reserva, datos));
    }

    /** Avisa a los productores que ven las reservas del día que se confirmó (pagó) una reserva. */
    public void notificarProductoresReservaConfirmada(Reserva reserva){
        notificarProductores(reserva, PermisoCodigo.GESTIONAR_ACTIVIDAD, TipoNotificacionNombre.RESERVA_CONFIRMADA_PRODUCTOR,
                enlaceReservasEstablecimiento(reserva), reserva.getReservaDetalles().size());
    }

    /**
     * Avisa a los productores que gestionan los reembolsos del nuevo estado de un reembolso: realizado, pendiente de
     * hacer por ellos, impago o pagado por el sistema. Los demás estados no se avisan.
     */
    public void notificarProductoresReembolso(Reembolso reembolso){
        TipoNotificacionNombre tipo = switch (reembolso.getEstadoActual().getEstadoReembolso().getNombre()) {
            case REEMBOLSADO_PRODUCTOR -> TipoNotificacionNombre.REEMBOLSO_REALIZADO_PRODUCTOR;
            case PEDIDO -> TipoNotificacionNombre.REEMBOLSO_PENDIENTE_PRODUCTOR;
            case IMPAGO -> TipoNotificacionNombre.REEMBOLSO_IMPAGO_PRODUCTOR;
            case REEMBOLSADO_SISTEMA -> TipoNotificacionNombre.REEMBOLSO_SISTEMA_PRODUCTOR;
            default -> null;
        };
        if (tipo == null) return;

        Reserva reserva = reembolso.getReserva();
        // El pendiente lleva a la lista donde se reintenta; el resto a las reservas
        String enlace = tipo == TipoNotificacionNombre.REEMBOLSO_PENDIENTE_PRODUCTOR
                ? RutasNotificacionesFront.reembolsosPendientesEstablecimiento(reserva.getActividad().getEstablecimiento().getId())
                : enlaceReservasEstablecimiento(reserva);

        notificarProductores(reserva, PermisoCodigo.GESTIONAR_REEMBOLSOS_PENDIENTES, tipo, enlace,
                reembolso.getMontoReembolso().toPlainString());
    }

    public String enlaceReservasEstablecimiento(Reserva reserva){
        return RutasNotificacionesFront.reservasEstablecimiento(reserva.getActividad().getEstablecimiento().getId());
    }

    /** Avisa a los administradores que pueden ver los reembolsos impagos que un reembolso pasó a "Impago". */
    public void notificarAdministradoresReembolsoImpago(Reembolso reembolso){
        Reserva reserva = reembolso.getReserva();
        notificacionService.notificarAdministradores(
                PermisoCodigo.LEER_REEMBOLSOS,
                TipoNotificacionNombre.REEMBOLSO_IMPAGO_ADMIN,
                RutasNotificacionesFront.reembolsoImpagoAdmin(reembolso.getId()),
                reserva.getActividad().getEstablecimiento().getNombre(),
                reembolso.getMontoReembolso().toPlainString(),
                reserva.getActividad().getNombre(),
                reserva.getActividadDia().getFechaHoraInicio().format(FORMATO_FECHA_HORA));
    }

    // Antepone a los datos del mensaje el nombre de la actividad y la fecha y hora del día de la reserva
    private Object[] datosMensajeReserva(Reserva reserva, Object... datos){
        Object[] datosMensaje = new Object[datos.length + 2];
        datosMensaje[0] = reserva.getActividad().getNombre();
        datosMensaje[1] = reserva.getActividadDia().getFechaHoraInicio().format(FORMATO_FECHA_HORA);
        System.arraycopy(datos, 0, datosMensaje, 2, datos.length);
        return datosMensaje;
    }
}
