package com.mza_agrotours.backend.entities.cuenta;


import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Pais extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String nombre;

    private LocalDate fechaBaja;

    @Column(nullable = false, unique = true)
    private String iso2;
}
