package com.mza_agrotours.backend.dtos.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatUsuarioDTO {
    private String establecimientoId;
    private String titulo;
    private String ultimoMensaje;
    private Long timestamp;
    private Integer mensajesNoLeidos;
}
