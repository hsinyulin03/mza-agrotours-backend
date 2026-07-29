package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.dtos.archivo.PresignedUrlRequest;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlResponse;
import com.mza_agrotours.backend.exceptions.ObjectStorageProviderException;

public interface ObjectStorageProvider {
    PresignedUrlResponse generatePresignedUrl(
            PresignedUrlRequest request) throws ObjectStorageProviderException;
}
