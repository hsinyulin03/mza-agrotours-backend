package com.mza_agrotours.backend.mappers;

import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.entities.Archivo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ArchivoMapper {
    List<Archivo> archivoUploadResponseListToArchivoList(List<ArchivoUploadResponse> archivos);
    Archivo archivoUploadResponseToArchivo(Archivo archivo);
}
