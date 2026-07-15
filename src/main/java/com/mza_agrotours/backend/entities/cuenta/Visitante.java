package com.mza_agrotours.backend.entities.cuenta;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Visitante extends BaseEntity {
    @OneToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToOne(optional = false)
    @JoinColumn(name = "pais_id", nullable = false)
    private Pais pais;
}
