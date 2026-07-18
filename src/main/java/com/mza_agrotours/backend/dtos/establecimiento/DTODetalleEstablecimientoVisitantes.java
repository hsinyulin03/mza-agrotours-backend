package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DTODetalleEstablecimientoVisitantes {

    private UUID id;

    private String nombre;

    private String razonSocial;

    private String departamento;

    private String descripcion;

    private String email;

    private String telefono;

    private String ubicacion;
// dto cultivos?
    private List<String> cultivos;

    private List<DTODetalleEstablecimientoActividad> actividades;
}
