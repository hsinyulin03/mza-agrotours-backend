package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

//US-ACT-12: Listado de actividades de la plataforma - vista del visitante
@Data
public class DTOListadoActividadVisitante {

        private UUID id;
        private String nombre;
        private BigDecimal precioRegular;

        //TODO: Valoraciones, imagenes,  cant de reseñas , nombre del establecimeinto,  cultivos y departamentos

}
