package com.mza_agrotours.backend.dtos.productor;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ProductorSuspenderReq {
    @NotBlank
    private String motivo;

    @NotNull
    @Future
    private LocalDateTime fechaHoraFinPrevista;
}
