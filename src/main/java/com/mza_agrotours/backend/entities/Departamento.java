package com.mza_agrotours.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Departamento extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String nombre;

    private String fechaBaja;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;

}