package com.mza_agrotours.backend.dtos.receta;

import lombok.*;

import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DTORectaBResponse {
    private UUID idReceta;
    private String mensaje;
}
