package com.mza_agrotours.backend.entities.establecimiento;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.security.cifrado.CifradoTokenConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Cuenta de Mercado Pago del establecimiento (vendedor), vinculada por OAuth a la app de Agrotours (marketplace).
 * <p></p>
 * Las preferencias de pago de las actividades del establecimiento se crean con su {@code accessToken},
 * así el cobro se acredita al vendedor y Agrotours recibe el {@code marketplace_fee}.
 * A lo sumo una cuenta por establecimiento tiene {@code fechaHoraBaja == null} (la vigente).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CuentaMercadoPago extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "establecimiento_id", nullable = false)
    private Establecimiento establecimiento;

    // Productor que autorizó la vinculación desde su cuenta de MP
    @ManyToOne(optional = false)
    @JoinColumn(name = "productor_id", nullable = false)
    private Productor vinculadaPor;

    @Column(nullable = false)
    private Long mpUserId;                      // user_id del vendedor en MP (collector_id de sus pagos)

    @Convert(converter = CifradoTokenConverter.class)
    @Column(nullable = false, length = 512)
    private String accessToken;

    @Convert(converter = CifradoTokenConverter.class)
    @Column(nullable = false, length = 512)
    private String refreshToken;

    @Column(length = 100)
    private String publicKey;

    @Column(nullable = false)
    private LocalDateTime fechaHoraExpiracionToken;

    @Column(nullable = false)
    private LocalDateTime fechaHoraAlta;

    private LocalDateTime fechaHoraUltimaRenovacion;

    private LocalDateTime fechaHoraBaja;        // Desvinculación o reemplazo por otra cuenta

    public boolean isVigente() {
        return fechaHoraBaja == null;
    }

    public boolean isTokenVencido(LocalDateTime ahora) {
        return !ahora.isBefore(fechaHoraExpiracionToken);
    }
}