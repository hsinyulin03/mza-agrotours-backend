package com.mza_agrotours.backend.dtos.PanelSistema;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstablecimientoAdminDto {
    private UUID id;
    private String nombre;


}
