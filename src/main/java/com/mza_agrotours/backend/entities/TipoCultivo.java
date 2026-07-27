package com.mza_agrotours.backend.entities;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TipoCultivo extends BaseEntity{
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaHoraBaja;
    //Falta agregar relaciones

}
