package com.mza_agrotours.backend.dtos.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatInfoUsuarioDTO {
    private String chatNombre;
    private String establecimientoNombre;
    private String urlChatFoto;
}
