package com.mza_agrotours.backend.dtos.tipoCultivo;

import lombok.Data;

import java.util.UUID;
@Data
public class DTOtcBResponse {
    private UUID idTipoCultivo;
    private String mensaje;
}
