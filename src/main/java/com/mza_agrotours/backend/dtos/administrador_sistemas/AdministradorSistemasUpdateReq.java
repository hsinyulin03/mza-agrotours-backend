package com.mza_agrotours.backend.dtos.administrador_sistemas;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AdministradorSistemasUpdateReq {
    @NotNull
    private UUID rolId;
}
