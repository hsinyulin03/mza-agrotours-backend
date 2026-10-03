package com.mza_agrotours.backend.dtos.chat;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatSnapshotDTO {
    private String visitanteId;
    private String establecimientoId;
    private Long creadoEl;
    private boolean baja;
}
