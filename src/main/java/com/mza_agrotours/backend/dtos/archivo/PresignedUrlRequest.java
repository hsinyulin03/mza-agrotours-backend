package com.mza_agrotours.backend.dtos.archivo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PresignedUrlRequest {
    @NotNull
    @Size(min = 1, max = 255)
    private String filename;

    @Size(max = 100)
    private String contentType;

    @Positive
    private long fileSize;
}