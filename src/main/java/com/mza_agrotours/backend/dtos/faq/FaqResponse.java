package com.mza_agrotours.backend.dtos.faq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FaqResponse {
    private UUID id;
    private String message;
}
