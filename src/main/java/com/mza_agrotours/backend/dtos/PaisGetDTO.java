package com.mza_agrotours.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaisGetDTO {
    private String nombre;
    private String iso2;
}
