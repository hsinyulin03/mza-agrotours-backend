package com.mza_agrotours.backend.dtos.calificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReseniaCardDTO {
    private String nombreUsuario;
    private LocalDateTime fechaHoraCalificacion;
    private Integer puntaje;
    private String resenia;
}
