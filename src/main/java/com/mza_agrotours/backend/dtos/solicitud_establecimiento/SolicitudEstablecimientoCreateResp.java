package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudEstablecimientoCreateResp {
    private String solicitudId;
    private String nombreEstablecimiento;
    private List<ArchivoUploadResponse> archivoUploadResponses;
}
