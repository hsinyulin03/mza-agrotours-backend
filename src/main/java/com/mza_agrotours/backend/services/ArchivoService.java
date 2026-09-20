package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlResponse;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.FailedToGenerateResourceSignedUrlException;
import com.mza_agrotours.backend.exceptions.ObjectStorageProviderException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchivoService {
    private final S3ObjectStorageService objectStorageService;

    public ArchivoService(S3ObjectStorageService objectStorageService) {
        this.objectStorageService = objectStorageService;
    }

    /**
     * Genera una lista de urls del object storage para una lista de archivos.
     * @param archivoUploadRequests lista de requests con el nombre del archivo
     * @param carpeta carpeta destino, que decide el prefijo y las extensiones validas
     * @return lista de archivos con la key y nombre del archivo
     * @throws DatoInvalidoException si el nombre del archivo no tiene una extension
     * @throws FailedToGenerateResourceSignedUrlException si no se pudo generar la url de un archivo
     */
    public List<ArchivoUploadResponse> getSignedArchivos(List<ArchivoUploadRequest> archivoUploadRequests, CarpetaArchivo carpeta) {
        return archivoUploadRequests
                .stream()
                .map(archivoUploadRequest ->
                        getSignedArchivo(archivoUploadRequest, carpeta))
                .toList();
    }

    /**
     * Genera una presignedUrl para un archivo del object storage.
     * @param archivoUploadRequest request con el nombre del archivo
     * @param carpeta carpeta destino, que decide el prefijo y las extensiones validas
     * @return archivo con la key y nombre del archivo
     * @throws DatoInvalidoException si el nombre del archivo no tiene una extension o
     * la extension no esta permitida en la carpeta.
     * @throws FailedToGenerateResourceSignedUrlException si no se pudo generar la url
     */
    public ArchivoUploadResponse getSignedArchivo(ArchivoUploadRequest archivoUploadRequest, CarpetaArchivo carpeta) {

        String filename = archivoUploadRequest.getFilename();
        String extension = getArchivoExtension(filename);

        if (extension == null) {
            throw new DatoInvalidoException("Archivo invalido: " + filename + " (sin extension)");
        }

        if (!carpeta.permite(extension)) {
            throw new DatoInvalidoException("Archivo invalido: " + filename
                    + " (extension no permitida en " + carpeta.getPrefijo() + ")");
        }

        PresignedUrlResponse presignedUrlResponse;
        try {
            presignedUrlResponse = objectStorageService
                    .generatePresignedUrl(archivoUploadRequest, carpeta);
        } catch (ObjectStorageProviderException e) {
            throw new FailedToGenerateResourceSignedUrlException(e.getMessage() + " (" + e.getCode() + ")");
        }

        return new ArchivoUploadResponse(
                presignedUrlResponse.getUploadUrl(),
                presignedUrlResponse.getKey(),
                extension,
                filename,
                presignedUrlResponse.getContentType()
        );
    }

    /**
     * Obtiene la extensión del archivo dado su nombre.
     * @param filename el nombre completo del archivo
     * @return la extensión del archivo (para `archivo.exe` retorna `exe`), si
     *          filename = null o filename = "" o filename = "archivo." o filename ="archivo",
     *          retorna null
     */
    private String getArchivoExtension(String filename) {
        if (filename == null ||
                filename.isEmpty() ||
                filename.lastIndexOf(".") == filename.length() - 1 ||
                filename.lastIndexOf(".") == -1) {
            return null;
        }

        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public String getDownloadUrl(String key) {
        return objectStorageService.generateDownloadUrl(key);
    }
}
