package com.mza_agrotours.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "object-storage")
public class ObjectStorageProperties {

    /**
     * Vacio para AWS S3; apunta a MinIO en desarrollo y en los tests. Cambiar
     * este valor y las credenciales alcanza para migrar a R2, GCS o B2.
     */
    private String endpoint;

    private String region;

    private String bucket;

    private String accessKey;

    private String secretKey;

    /**
     * MinIO resuelve el bucket por path; AWS S3 lo resuelve por subdominio.
     */
    private boolean pathStyleAccess;

    /**
     * Raiz publica del bucket, sin barra final: lo que se antepone a la key de
     * un objeto de una carpeta publica. No se deriva del endpoint porque no es
     * la misma forma en todos lados (path style en MinIO, subdominio en AWS S3,
     * un dominio propio si algun dia hay CDN adelante).
     * <p>
     * Vacio deja todo firmado, incluso lo que esta en una carpeta publica: sin
     * este valor no hay url publica que devolver.
     */
    private String publicBaseUrl;

    private Duration presignTtl = Duration.ofMinutes(15);

    private long maxFileSize;
}
