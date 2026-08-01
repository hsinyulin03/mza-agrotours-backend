package com.mza_agrotours.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Archivo extends BaseEntity{
    @Column(nullable = false, unique = true)
    private String key;

    @Column(nullable = false)
    private String nombre;

    // fixme cómo me aseguro que coincida con el archivo real?
    @Column(nullable = false)
    private String extension;
}
