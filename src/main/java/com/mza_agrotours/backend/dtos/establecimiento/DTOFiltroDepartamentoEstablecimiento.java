package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DTOFiltroDepartamentoEstablecimiento {
    private UUID id;
    private String nombre;
    private Long cantidadEstablecimientos;
}

