package com.mza_agrotours.backend.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActividadFoto extends BaseEntity {
    @Column(nullable = false)
    private Integer orden;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private Archivo archivo;
}
