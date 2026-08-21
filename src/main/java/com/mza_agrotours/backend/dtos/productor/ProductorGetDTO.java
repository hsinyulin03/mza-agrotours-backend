package com.mza_agrotours.backend.dtos.productor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class ProductorGetDTO {
    private String id;
    private String nombreUsuario;
    private String emailUsuario;
    private String identificacion;
    private String nombreRol;
    private Boolean esLider;
    private LocalDateTime fechaHoraAlta;

    private String estadoActual;

    // Solo viaja cuando el productor esta suspendido: es el vencimiento planificado
    // del tramo vigente, no su cierre efectivo.
    private LocalDateTime fechaHoraFinSuspension;
}
