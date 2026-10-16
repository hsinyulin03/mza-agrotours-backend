package com.mza_agrotours.backend.dtos.actividad;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActividadReprogramarRequest {

    @Size(max=200)
    @NotBlank(message = "El motivo es requerido")
    private String motivo;

    @NotNull
    @Future
    private LocalDateTime fechaHoraInicioReprogramada;

    @NotNull
    private LocalTime horaFinReprogramada;
}
