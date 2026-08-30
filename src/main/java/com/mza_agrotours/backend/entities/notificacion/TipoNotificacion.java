package com.mza_agrotours.backend.entities.notificacion;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.TipoNotificacionNombre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TipoNotificacion extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private TipoNotificacionNombre nombre;

}
