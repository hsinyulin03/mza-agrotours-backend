package com.mza_agrotours.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectStorageConfig {
    @Bean
    public ObjectStorageProvider objectStorageProvider(
            @Value("${object-storage.provider}") String provider) {
        return switch (provider.toLowerCase()) {
            case "local" -> new LocalObjectStorageProvider();
            case "s3" -> new S3ObjectStorageProvider();
            default -> throw new IllegalStateException(
                    "object-storage.provider desconocido: " + provider);
        };
    }
}