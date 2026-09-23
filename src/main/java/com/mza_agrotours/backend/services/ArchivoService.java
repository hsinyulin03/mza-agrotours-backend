package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.archivo.ArchivoClaimRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadRequest;
import com.mza_agrotours.backend.dtos.archivo.ArchivoUploadResponse;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlResponse;
import com.mza_agrotours.backend.entities.Archivo;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.exceptions.FailedToGenerateResourceSignedUrlException;
import com.mza_agrotours.backend.exceptions.ObjectStorageProviderException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import com.mza_agrotours.backend.repositories.ArchivoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArchivoService {
    private final S3ObjectStorageService objectStorageService;
    private final ArchivoRepository archivoRepository;

    public ArchivoService(S3ObjectStorageService objectStorageService, ArchivoRepository archivoRepository) {
        this.objectStorageService = objectStorageService;
        this.archivoRepository = archivoRepository;
    }

    /**
     * Genera una lista de urls de subida para una lista de archivos.
     * @param archivoUploadRequests lista de requests con el nombre del archivo
     * @param carpeta carpeta destino, que decide el prefijo y las extensiones validas
     * @return lista de archivos con la key y nombre del archivo
     * @throws DatoInvalidoException si el nombre del archivo no tiene una extension
     * @throws FailedToGenerateResourceSignedUrlException si no se pudo generar la url de un archivo
     */
    public List<ArchivoUploadResponse> getSignedArchivos(List<ArchivoUploadRequest> archivoUploadRequests, CarpetaArchivo carpeta) {
        return archivoUploadRequests
                .stream()
                .map(archivoUploadRequest -> getSignedArchivo(archivoUploadRequest, carpeta))
                .toList();
    }

    /**
     * Genera una presignedUrl para un archivo.
     * @param archivoUploadRequest request con el nombre y el tamanio declarado del archivo
     * @param carpeta carpeta destino, que decide el prefijo y las extensiones validas
     * @return archivo con la url de subida, la key y el content type a enviar en el PUT
     * @throws DatoInvalidoException si el nombre no tiene extension, la extension no esta
     * permitida en la carpeta, o el tamanio declarado la excede
     * @throws FailedToGenerateResourceSignedUrlException si no se pudo generar la url
     */
    public ArchivoUploadResponse getSignedArchivo(ArchivoUploadRequest archivoUploadRequest, CarpetaArchivo carpeta) {
        String filename = archivoUploadRequest.getFilename();
        String extension = extensionValidada(filename, carpeta);

        if (archivoUploadRequest.getFileSize() > carpeta.getMaxFileSize()) {
            throw new DatoInvalidoException("El archivo " + filename + " supera el tamanio maximo de "
                    + carpeta.getMaxFileSize() + " bytes para " + carpeta.getPrefijo());
        }

        PresignedUrlResponse presignedUrlResponse;
        try {
            presignedUrlResponse = this.objectStorageService.generatePresignedUrl(archivoUploadRequest, carpeta);
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
     * Convierte en entidades las keys que el cliente dice haber subido. Es el
     * unico punto donde el backend confia en algo que vino del cliente, asi que
     * verifica que la key la haya emitido el, que caiga en la carpeta correcta,
     * que nadie mas la haya reclamado y que el objeto exista con el peso debido.
     *
     * @throws DatoInvalidoException si la key no corresponde a la carpeta, su extension
     * no esta permitida, ya fue reclamada, o el objeto excede el tamanio de la carpeta
     * @throws ResourceNotFoundException si el objeto nunca llego al bucket
     */
    public List<Archivo> reclamarArchivos(List<ArchivoClaimRequest> claims, CarpetaArchivo carpeta) {
        if (claims == null || claims.isEmpty()) {
            return List.of();
        }

        return claims.stream()
                .map(claim -> reclamarArchivo(claim, carpeta))
                .toList();
    }

    public Archivo reclamarArchivo(ArchivoClaimRequest claim, CarpetaArchivo carpeta) {
        String key = claim.getKey();

        if (!ObjectStorageKeys.isValid(key) || !carpeta.contiene(key)) {
            throw new DatoInvalidoException("Archivo invalido: " + key
                    + " no es una key de " + carpeta.getPrefijo());
        }

        String extension = extensionValidada(key, carpeta);

        if (this.archivoRepository.existsByKey(key)) {
            throw new DatoInvalidoException("Archivo invalido: " + key + " ya fue asociado a otro registro");
        }

        long tamanio = this.objectStorageService.tamanioDe(key);
        if (tamanio > carpeta.getMaxFileSize()) {
            throw new DatoInvalidoException("El archivo " + claim.getNombre() + " pesa " + tamanio
                    + " bytes y supera el maximo de " + carpeta.getMaxFileSize() + " para " + carpeta.getPrefijo());
        }

        return new Archivo(key, claim.getNombre(), extension);
    }

    public String getDownloadUrl(String key) {
        return this.objectStorageService.generateDownloadUrl(key);
    }

    private String extensionValidada(String nombreOKey, CarpetaArchivo carpeta) {
        String extension = getArchivoExtension(nombreOKey);

        if (extension == null) {
            throw new DatoInvalidoException("Archivo invalido: " + nombreOKey + " (sin extension)");
        }

        if (!carpeta.permite(extension)) {
            throw new DatoInvalidoException("Archivo invalido: " + nombreOKey
                    + " (extension no permitida en " + carpeta.getPrefijo() + ")");
        }

        return extension;
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
