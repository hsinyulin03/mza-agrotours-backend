package com.mza_agrotours.backend.dtos.solicitud_establecimiento;

import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SolicitudEstablecimientoCreateReq {
    @NotNull
    @Size(min = 1, max = 100)
    private String nombreEstablecimiento;

    @NotNull
    @Size(min = 1, max = 100)
    private String razonSocial;

    @NotNull
    @Size(min = 1, max = 11)
    private String cuit;

    @NotBlank
    @Size(min = 1, max = 2000)
    private String descripcion;

    @NotNull
    @Size(min = 1, max = 200)
    private String domicilioLegal;

    @NotNull
    private String departamento;

    @NotNull
    @Size(min = 7, max = 16)
    private String telefono;

    @NotNull
    @Size(min = 1, max = 100)
    private String email;

    @NotNull
    @Size(min = 22, max = 22)
    private String cvu;

    @Size(min = 1, max = 10)
    private List<ArchivoUploadRequest> archivos;
}
