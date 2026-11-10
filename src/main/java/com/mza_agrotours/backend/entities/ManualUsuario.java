package com.mza_agrotours.backend.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hay una unica fila: el manual vigente. Subir uno nuevo reemplaza el archivo
 * de esa fila en vez de crear otra.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ManualUsuario extends BaseEntity {
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "archivo_id")
    private Archivo archivo;
}
