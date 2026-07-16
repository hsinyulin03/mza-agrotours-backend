package com.mza_agrotours.backend.entities.actividad;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actividad_log_altas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ActividadLogAltas extends BaseEntity {

    private LocalDateTime fechaHoraAlta;
    private LocalDate fechaValidaDesde;
    private LocalDate fechaValidaHasta;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "actividad_log_altas_id")
    private List<ActividadLogAltasDia> dias = new ArrayList<>();

    public void addDia(ActividadLogAltasDia dia) {
        this.dias.add(dia);
    }
}
