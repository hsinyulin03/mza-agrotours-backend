package com.mza_agrotours.backend.dtos.archivo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArchivoUploadResponse{
    private String uploadUrl;
    private String key;
    private String extension;
    private String nombre;
}
