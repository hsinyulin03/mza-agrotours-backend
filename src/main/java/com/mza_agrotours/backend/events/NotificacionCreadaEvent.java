package com.mza_agrotours.backend.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class NotificacionCreadaEvent {
    private final UUID notificacionId;
}
