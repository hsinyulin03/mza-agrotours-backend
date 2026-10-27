package com.mza_agrotours.backend.dtos.establecimiento;

import com.mza_agrotours.backend.dtos.archivo.DTOFotosResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class DTODetalleEstablecimientoActividad {
    private UUID id;

    private String nombre;

    private List<DTOCultivoEstablecimientoResponse> cultivos;

    private BigDecimal precioDesde;

    private Float calificacionPromedio;
    private Integer cantidadResenias;

    private DTOFotosResponse fotoPortada;
}
