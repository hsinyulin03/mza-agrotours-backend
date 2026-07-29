package com.mza_agrotours.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdministradorSistemas extends BaseEntity {
    private LocalDateTime fechaHoraAlta;
    private LocalDateTime fechaHoraBaja;

    @OneToOne(optional = false)
    private Usuario usuario;
}
