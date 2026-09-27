package com.mza_agrotours.backend.dtos.archivo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class PresignArchivosRequest {
    @NotEmpty
    @Size(max = 20)
    @Valid
    private List<ArchivoUploadRequest> archivos;
}
