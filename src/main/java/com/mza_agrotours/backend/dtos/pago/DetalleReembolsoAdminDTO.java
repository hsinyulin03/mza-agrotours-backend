package com.mza_agrotours.backend.dtos.pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DetalleReembolsoAdminDTO(
        // Reembolso - id, fechaHoraPedido, monto
        String idReembolso, LocalDateTime fechaHoraPedido, BigDecimal montoReembolso,

        // ReembolsoEstado - estado actual y desde cuándo está en él
        String estado, LocalDateTime fechaHoraEstado,

        // Establecimiento - nombre
        String nombreEstablecimiento,

        // Reserva, actividad y día reservado
        String idReserva, String nombreActividad,
        LocalDateTime actividadFechaHoraInicio, LocalDateTime actividadFechaHoraFin,

        // Contactos para resolver el reembolso
        ContactoReembolsoDTO visitante, ContactoReembolsoDTO productorLider,

        // Personas incluidas en la reserva, por renglón
        List<PersonaReservaDTO> personas,

        // Pago con el que el sistema hizo el reembolso. Null mientras está IMPAGO
        PagoReembolsoSistemaDTO pagoReembolsoSistema
) {

}
