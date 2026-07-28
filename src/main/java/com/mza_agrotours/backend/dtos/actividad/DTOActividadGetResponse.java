package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class DTOActividadGetResponse {
    private UUID id;
    private String nombre;
    private String descripcion;

    //TODO: Agregar relacion con cultivos, imagenes

    private List<DTOTarifaResponse> rangosEtarios;
    private List<String> incluye;
    private List<String> noIncluye;
    private List<DTOFaqResponse> faqs;
    private String estado;
    List<String> advertencias;

}
