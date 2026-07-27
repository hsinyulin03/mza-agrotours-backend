package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DTOActividadGetResponse {
    private String nombre;
    private String descripcion;

    /*Me parece mejor no incluirlo porque no afecta en nada, al menos que se quiera usar como cupo base, pero mucho sentido no le veo
    @NotNull(message = "El cupo máximo es obligatorio")
    @Min(value = 1, message = "El cupo mínimo debe ser 1")
    private Integer cuposMaximos;*/
    //TODO: Agregar relacion con cultivos, imagenes

    private List<DTOTarifaResponse> rangosEtarios;
    private List<String> incluye;
    private List<String> noIncluye;
    private List<DTOFaqResponse> faqs;
    private String estado;
    List<String> advertencias;

}
