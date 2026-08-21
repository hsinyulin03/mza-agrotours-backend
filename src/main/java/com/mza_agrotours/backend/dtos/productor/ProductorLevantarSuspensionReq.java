package com.mza_agrotours.backend.dtos.productor;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductorLevantarSuspensionReq {
    @NotBlank
    private String motivo;
}
