package com.mza_agrotours.backend.dtos.productor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class ProductorUpdateReq {
    @NotNull
    private UUID rolId;
}
