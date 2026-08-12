package com.mza_agrotours.backend.dtos.receta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DTORecetaDetalleCultivoM {
    private UUID id;
    private String nombre;
}
