package com.mza_agrotours.backend.dtos.establecimiento;

import lombok.Data;

import java.util.List;

@Data
public class DTODatosEstablecimiento {
    // Identidad
    private String id;
    private String nombre;
    private String cuit;
    private String razonSocial;
    private String descripcion;
    //Ubicacion
    private String ubicacion;
    private String localidad;
    //Contacto
    private String telefono;
    private String email;
    //Operacion
    private String cvu;
    // Cultivos
    private List<DTODatosEstablecimientoCultivos> cultivos;
}
