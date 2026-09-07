package com.mza_agrotours.backend.dtos.notificacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TokenNotificacionReqDTO {
    @NotBlank
    @Size(max=512)
    private String token;
}

