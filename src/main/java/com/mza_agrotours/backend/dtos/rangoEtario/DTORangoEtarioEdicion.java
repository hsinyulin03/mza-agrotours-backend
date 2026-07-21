package com.mza_agrotours.backend.dtos.rangoEtario;

import java.util.UUID;

public class DTORangoEtarioEdicion {
    private UUID id;
    private String nombre;
    private Integer edadMinima;
    private Integer edadMaxima;
    //indicador que la plataforma dio de baja ese rango en el catálogo general
    private boolean obsoleto;
}
