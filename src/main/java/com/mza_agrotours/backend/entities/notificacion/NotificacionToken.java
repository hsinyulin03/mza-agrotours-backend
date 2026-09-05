package com.mza_agrotours.backend.entities.notificacion;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionToken extends BaseEntity {

    @Column(nullable = false)
    private LocalDateTime fechaHoraAlta;

    private LocalDateTime fechaHoraBaja;

    @ManyToOne(optional = false)
    @JoinColumn(name = "token_notificacion_id", nullable = false)
    private TokenNotificacion tokenNotificacion;

    @ManyToOne(optional = false)
    private EstadoNotificacionToken estadoNotificacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "notificacion_id", nullable = false)
    private Notificacion notificacion;
}
