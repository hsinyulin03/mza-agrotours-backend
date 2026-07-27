package com.mza_agrotours.backend.config;

import com.mza_agrotours.backend.dtos.archivo.PresignedUrlRequest;
import com.mza_agrotours.backend.dtos.archivo.PresignedUrlResponse;
import com.mza_agrotours.backend.services.ObjectStorageKeys;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Proveedor de desarrollo: las "presigned urls" apuntan al propio backend,
 * que sirve y recibe los bytes desde el disco local.
 */
public class LocalObjectStorageProvider implements ObjectStorageProvider {

    @Override
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        String key = ObjectStorageKeys.generate(request.getFilename());
        String uploadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/object-storage/objects/{key}")
                .buildAndExpand(key)
                .toUriString();

        return new PresignedUrlResponse(uploadUrl, key);
    }
}
