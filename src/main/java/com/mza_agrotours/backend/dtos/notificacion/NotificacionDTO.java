package com.mza_agrotours.backend.dtos.notificacion;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificacionDTO {
    private String id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String urlLink;
    private LocalDateTime fechaHoraAlta;
    private boolean leida;
    private String establecimientoId;
}
