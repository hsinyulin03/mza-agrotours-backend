package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class DTOActividadGetResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private List<DTOCultivoResponse> cultivos;
    private List<ArchivoUploadResponse> fotosParaSubir;
    private List<DTOFotosResponse> fotosGuardadas;
    private List<DTOTarifaResponse> rangosEtarios;
    private List<String> incluye;
    private List<String> noIncluye;
    private List<DTOFaqResponse> faqs;
    private String estado;
    List<String> advertencias;

}
