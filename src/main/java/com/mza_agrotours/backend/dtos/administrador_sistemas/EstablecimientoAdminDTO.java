package com.mza_agrotours.backend.dtos.administrador_sistemas;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
public class EstablecimientoAdminDTO {
    private String id;
    private String nombre;
    private String productorLider;
    private String departamento;
    private LocalDate fechaAlta;

    private Long cantidadActividadesPublicadas;
    private Long cantidadReservasHistorico;

    // Información del estado
    private String estado;
    private String motivoEstado;
    private LocalDateTime fechaEstado;
}
