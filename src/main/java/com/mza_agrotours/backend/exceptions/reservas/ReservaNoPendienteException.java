package com.mza_agrotours.backend.exceptions.reservas;

import java.util.UUID;

/**
 * La reserva dejó de estar "Pendiente" mientras se procesaba (otro proceso ya la pagó, expiró o canceló).
 */
public class ReservaNoPendienteException extends RuntimeException {
    public ReservaNoPendienteException(UUID reservaId) {
        super("La reserva " + reservaId + " ya no está pendiente");
    }
}
