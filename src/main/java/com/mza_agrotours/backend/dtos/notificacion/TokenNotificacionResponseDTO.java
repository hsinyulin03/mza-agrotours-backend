package com.mza_agrotours.backend.dtos.notificacion;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TokenNotificacionResponseDTO {
    private String id;
    private String token;
    private LocalDateTime fechaHoraAlta;
    private LocalDateTime fechaHoraBaja;
}
