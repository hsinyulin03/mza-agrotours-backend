package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.dtos.archivo.PresignedUrlRequest;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlResponse;

/**
 * TODO: implementar con el SDK de S3.
 */
public class S3ObjectStorageProvider implements ObjectStorageProvider {
    @Override
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        throw new UnsupportedOperationException("Proveedor S3 no implementado todavia");
    }

    @Override
    public String generateDownloadUrl(String key) {
        throw new UnsupportedOperationException("Proveedor S3 no implementado todavia");
    }
}