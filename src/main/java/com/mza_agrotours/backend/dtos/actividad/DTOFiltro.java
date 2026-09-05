package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class DTOFiltro {
    private String valor;  // "BORRADOR" o el UUID como string
    private String nombre;
    private long cantidad;

    // Constructor para Hibernate (cuando lee el UUID de la base de datos)
    public DTOFiltro(UUID valorUuid, String nombre, long cantidad) {
        this.valor = valorUuid != null ? valorUuid.toString() : null;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    // Constructor genérico (para los Enum de los Estados)
    public DTOFiltro(String valorString, String nombre, long cantidad) {
        this.valor = valorString;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }
    public DTOFiltro(EstadoActividadNombre estadoEnum, long cantidad) {
        this.valor = estadoEnum.name(); // Transforma BORRADOR a "BORRADOR"
        this.cantidad = cantidad;
    }
}
