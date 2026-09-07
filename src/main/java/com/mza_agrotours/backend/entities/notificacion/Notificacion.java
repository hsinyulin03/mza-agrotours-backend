package com.mza_agrotours.backend.entities.notificacion;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion extends BaseEntity {
    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    private String urlLink;

    @Column(nullable = false)
    private LocalDateTime fechaHoraAlta;

    private LocalDateTime fechaHoraLectura;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @ManyToOne(optional = false)
    private TipoNotificacion tipoNotificacion;

    @ManyToOne
    @JoinColumn(name = "establecimiento_id")
    private Establecimiento establecimiento;   // null = notificación personal

}
