package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.config.ObjectStorageProvider;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlResponse;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.FailedToGenerateResourceSignedUrlException;
import com.mza_agrotours.backend.exceptions.ObjectStorageProviderException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchivoService {
    private final ObjectStorageProvider objectStorageProvider;

    public ArchivoService(ObjectStorageProvider objectStorageProvider) {
        this.objectStorageProvider = objectStorageProvider;
    }

    /**
     * Genera una lista de urls del object storage provider para una lista de archivos.
     * @param archivoUploadRequests lista de requests con el nombre del archivo
     * @return lista de archivos con la key y nombre del archivo
     * @throws DatoInvalidoException si el nombre del archivo no tiene una extension
     * @throws FailedToGenerateResourceSignedUrlException si no se pudo generar la url de un archivo
     */
    public List<ArchivoUploadResponse> getSignedArchivos(List<ArchivoUploadRequest> archivoUploadRequests, List<String> allowedExtensions) {
        return archivoUploadRequests
                .stream()
                .map(archivoUploadRequest ->
                        getSignedArchivo(archivoUploadRequest, allowedExtensions))
                .toList();
    }

    /**
     * Genera una presignedUrl para un archivo del object storage provider.
     * @param archivoUploadRequest request con el nombre del archivo
     * @return archivo con la key y nombre del archivo
     * @throws DatoInvalidoException si el nombre del archivo no tiene una extension o
     * la extension no esta en la lista de extensiones permitidas.
     * @throws FailedToGenerateResourceSignedUrlException si no se pudo generar la url
     */
    public ArchivoUploadResponse getSignedArchivo(ArchivoUploadRequest archivoUploadRequest, List<String> allowedExtensions) {

        String filename = archivoUploadRequest.getFilename();
        String extension = getArchivoExtension(filename);

        PresignedUrlResponse presignedUrlResponse;
        try {
            presignedUrlResponse = objectStorageProvider
                    .generatePresignedUrl(archivoUploadRequest);
        } catch (ObjectStorageProviderException e) {
            throw new FailedToGenerateResourceSignedUrlException(e.getMessage() + e.getCode());
        }

        String key = presignedUrlResponse.getKey();
        String uploadUrl = presignedUrlResponse.getUploadUrl();


        if (extension == null) {
            throw new DatoInvalidoException("Archivo invalido: " + filename + " (sin extension)");
        }

        if (allowedExtensions != null  && !allowedExtensions.contains(extension.toLowerCase())) {
            throw new DatoInvalidoException("Archivo invalido: " + filename + " (extension no permitida)");
        }

        return new ArchivoUploadResponse(
                uploadUrl,
                key,
                extension,
                filename
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
}
