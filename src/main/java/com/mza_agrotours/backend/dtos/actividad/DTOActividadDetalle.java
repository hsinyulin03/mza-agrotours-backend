package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.util.List;

@Data

//US-ACT-02: Consultar detalle de una actividad

public class DTOActividadDetalle {
    //Informacion General
    private String nombre;
    /* No se si incluir porque puede tener actividadDia con distintas duraciones
    private Integer duracionEstimadaHoras;*/

    private Integer cuposMax;

    /*Y con respecto a esto también no lo veo muy necesario incluir
    private Integer edadMinima;*/

    //Sobre la experiencia
    private String descripcion;
    private List<String> incluye;
    private List<String> noIncluye;

    // Preguntas frecuentes
    private List<DTOFaqResponse> preguntasFrecuentes;


    //TODO- Falta mostrar tipo de  cultivo, información del establecimiento, ubicación, pronóstico, reseñas y calendario para reservas
}
