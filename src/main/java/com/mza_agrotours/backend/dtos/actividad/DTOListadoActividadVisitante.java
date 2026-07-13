package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.math.BigDecimal;

//US-ACT-12: Listado de actividades de la plataforma - vista del visitante
@Data
public class DTOListadoActividadVisitante {


        private Long id;
        private String nombre;
        private BigDecimal precioDesde;

        //TODO: Valoraciones, imagenes,  cant de reseñas , nombre del establecimeinto,  cultivos y departamentos

}
