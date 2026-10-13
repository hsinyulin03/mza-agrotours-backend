package com.mza_agrotours.backend.dtos.PanelProductor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendientesDTO {
    private long reembolsosPendientes;
    private LocalDateTime reembolsoMasAntiguo;
    private long actividadesBorrador;
    private boolean mercadoPagoVinculado;
    private LocalDateTime mercadoPagoTokenExpira;
}
