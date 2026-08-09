package com.mza_agrotours.backend.dtos.receta;

import lombok.Data;

import java.util.UUID;

@Data
public class DTORecetaAMResponse {
    private UUID idReceta;
    private String mensaje;

}
