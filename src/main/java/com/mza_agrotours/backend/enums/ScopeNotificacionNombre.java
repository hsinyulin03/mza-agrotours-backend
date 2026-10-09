package com.mza_agrotours.backend.enums;

/**
 * Bandeja a la que pertenece una notificacion. Solo ESTABLECIMIENTO lleva
 * establecimiento asociado; VISITANTE y ADMINISTRADOR no.
 */
public enum ScopeNotificacionNombre {
    VISITANTE,
    ESTABLECIMIENTO,
    ADMINISTRADOR
}
