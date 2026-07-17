package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data

//US-ACT-02: Consultar detalle de una actividad

public class DTOActividadDetalleResponse {
    private UUID id;
    //Informacion General
    private String nombre;
    private Integer cuposMax;

    //Sobre la experiencia
    private String descripcion;
    private List<String> incluye;
    private List<String> noIncluye;

    // Preguntas frecuentes
    private List<DTOFaqResponse> preguntasFrecuentes;


    //TODO- Falta mostrar tipo de  cultivo, información del establecimiento, ubicación, pronóstico, reseñas y calendario para reservas
}
