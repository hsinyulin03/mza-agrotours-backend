package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.EstadoActividad;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

//US-ACT-06: Listado de actividades de un establecimiento - Vista productor
@Data
public class DTOActividades {
    private String nombre;
    private EstadoActividad estado;
    private BigDecimal precioRegular;
    private List<String> diasYHorasDisponibles;
    /* TODO- Ver si es necesario incluirlo
    private int cantidadReservasAsociadas;*/
    //TODO-Se debe filtrar por establecimiento

}
