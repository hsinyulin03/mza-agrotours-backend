package com.mza_agrotours.backend.dtos.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ChatInfoRequest {
    @NotNull
    private UUID actividadId;

    private String usuarioFirebaseId;
}
