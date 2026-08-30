package com.mza_agrotours.backend.dtos.notificacion;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class NotificacionCreadaEvent {
    private final UUID notificacionId;
}
