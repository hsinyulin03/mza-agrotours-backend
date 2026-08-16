package com.mza_agrotours.backend.dtos.actividad;

import lombok.Data;

@Data
public class DTOFotosResponse {
    private String key;
    private String nombre;
    private String extension;
    private String downloadUrl; //para descargar la imagen
}
