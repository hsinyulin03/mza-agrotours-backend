package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.config.ObjectStorageProperties;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlRequest;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlResponse;
import com.mza_agrotours.backend.exceptions.DatoInvalidoException;
import com.mza_agrotours.backend.enums.CarpetaArchivo;
import com.mza_agrotours.backend.exceptions.ObjectStorageProviderException;
import com.mza_agrotours.backend.exceptions.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.util.Optional;

/**
 * Urls prefirmadas contra un storage compatible con S3: MinIO en desarrollo y
 * en los tests, AWS S3 en produccion. Los bytes nunca pasan por el backend.
 */
@Service
public class S3ObjectStorageService {
    private final S3Presigner presigner;
    private final S3Client s3Client;
    private final ObjectStorageProperties properties;

    public S3ObjectStorageService(S3Presigner presigner, S3Client s3Client, ObjectStorageProperties properties) {
        this.presigner = presigner;
        this.s3Client = s3Client;
        this.properties = properties;
    }

    /**
     * @throws DatoInvalidoException si el tamanio declarado supera el maximo
     * @throws ObjectStorageProviderException si el proveedor no pudo firmar la url
     */
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request, CarpetaArchivo carpeta)
            throws ObjectStorageProviderException {
        if (request.getFileSize() > this.properties.getMaxFileSize()) {
            throw new DatoInvalidoException("El archivo " + request.getFilename()
                    + " supera el tamanio maximo permitido de " + this.properties.getMaxFileSize() + " bytes");
        }

        String key = ObjectStorageKeys.generate(carpeta, request.getFilename());
        String contentType = contentTypeOf(key);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(this.properties.getBucket())
                .key(key)
                .contentType(contentType)
                .build();

        try {
            String uploadUrl = this.presigner.presignPutObject(PutObjectPresignRequest.builder()
                            .signatureDuration(this.properties.getPresignTtl())
                            .putObjectRequest(putObjectRequest)
                            .build())
                    .url()
                    .toString();

            return new PresignedUrlResponse(uploadUrl, key, contentType);
        } catch (SdkException e) {
            throw new ObjectStorageProviderException(e.getMessage(), e.getClass().getSimpleName());
        }
    }

    /**
     * Url de lectura de un objeto: directa y permanente si cae en una carpeta
     * publica, prefirmada y con vencimiento en cualquier otro caso.
     *
     * @throws DatoInvalidoException si la key no la emitio el servidor
     */
    public String generateDownloadUrl(String key) {
        if (!ObjectStorageKeys.isValid(key)) {
            throw new DatoInvalidoException("Key de objeto invalida: " + key);
        }

        return urlPublicaDe(key).orElseGet(() -> presignedUrlDe(key));
    }

    /**
     * Vacio si la carpeta del objeto no es publica, o si no hay raiz publica
     * configurada: en ambos casos el objeto se sirve firmado.
     */
    private Optional<String> urlPublicaDe(String key) {
        if (!StringUtils.hasText(this.properties.getPublicBaseUrl())) {
            return Optional.empty();
        }

        return CarpetaArchivo.de(key)
                .filter(CarpetaArchivo::isPublica)
                .map(carpeta -> StringUtils.trimTrailingCharacter(this.properties.getPublicBaseUrl(), '/')
                        + "/" + key);
    }

    private String presignedUrlDe(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.properties.getBucket())
                .key(key)
                .build();

        return this.presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(this.properties.getPresignTtl())
                        .getObjectRequest(getObjectRequest)
                        .build())
                .url()
                .toString();
    }

    /**
     * Tamanio real del objeto ya subido. Es la unica forma de saber cuanto
     * pesa: la url prefirmada no puede imponer un limite, asi que lo que el
     * cliente declaro al pedirla es apenas una promesa.
     *
     * @throws ResourceNotFoundException si el objeto no esta en el bucket
     */
    public long tamanioDe(String key) {
        try {
            return this.s3Client.headObject(request -> request
                            .bucket(this.properties.getBucket())
                            .key(key))
                    .contentLength();
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("No existe el objeto " + key);
        }
    }

    /**
     * El content type lo decide el servidor a partir de la extension y va
     * firmado en la url: el cliente no puede guardar un objeto declarando un
     * tipo que no le corresponde.
     */
    private String contentTypeOf(String key) {
        return MediaTypeFactory.getMediaType(key)
                .orElse(MediaType.APPLICATION_OCTET_STREAM)
                .toString();
    }
}
