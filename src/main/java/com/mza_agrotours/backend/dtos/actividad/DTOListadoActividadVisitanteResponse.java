package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

//US-ACT-12: Listado de actividades de la plataforma - vista del visitante
@Data
public class DTOListadoActividadVisitanteResponse {

        private UUID id;
        private String nombre;
        private BigDecimal precioRegular;
        private List<DTOCultivoResponse> cultivos;
        private DTOFotosResponse fotoPortada;
        private String nombreEstablecimiento;
        private String nombreDepartamento;

        //TODO: Valoraciones,  cant de reseñas

}
