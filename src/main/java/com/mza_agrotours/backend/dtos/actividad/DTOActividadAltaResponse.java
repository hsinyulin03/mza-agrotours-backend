package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DTOActividadAltaResponse {
    UUID idActividad;
    String mensaje;
    List<String> advertencias;
    private List<ArchivoUploadResponse> archivoUploadResponses;
}
