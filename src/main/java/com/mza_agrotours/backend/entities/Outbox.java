package com.mza_agrotours.backend.entities;

import com.mza_agrotours.backend.enums.outbox.EstadoOutbox;
import com.mza_agrotours.backend.enums.outbox.TipoOperacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Outbox extends BaseEntity {
    @Column(nullable = false)
    private String entidadId;

    @Column(nullable = false)
    private LocalDateTime fechaHoraAlta;

    @Column(nullable = false)
    private Integer reintentos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOutbox estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOperacion operacion;

    @Column(nullable = false)
    private LocalDateTime fechaHoraProximoIntento;

}
