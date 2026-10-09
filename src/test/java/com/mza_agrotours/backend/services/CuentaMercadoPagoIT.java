package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.repositories.CuentaMercadoPagoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.support.AbstractIntegrationTest;
import com.mza_agrotours.backend.support.FixtureEstablecimiento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class CuentaMercadoPagoIT extends AbstractIntegrationTest {
    @Autowired private EstablecimientoService establecimientoService;
    @Autowired private FixtureEstablecimiento fixtureEstablecimiento;
    @Autowired private EstablecimientoRepository establecimientoRepository;
    @Autowired private CuentaMercadoPagoRepository cuentaMercadoPagoRepository;

    private Establecimiento conCuentaVinculada(LocalDateTime vencimientoToken) {
        Establecimiento establecimiento = fixtureEstablecimiento.establecimientoActivo();

        CuentaMercadoPago cuenta = new CuentaMercadoPago();
        cuenta.setVinculadaPor(establecimiento.getTitular());
        cuenta.setMpUserId(123456L);
        cuenta.setAccessToken("APP_USR-access");
        cuenta.setRefreshToken("TG-refresh");
        cuenta.setFechaHoraExpiracionToken(vencimientoToken);
        establecimiento.vincularCuentaMercadoPago(cuenta, LocalDateTime.now());

        return establecimientoRepository.save(establecimiento);
    }

    @Test
    void dadoEstablecimientoConCuentaMp_cuandoSeDaDeBaja_entoncesSeDesvinculaLaCuenta() {
        Establecimiento establecimiento = conCuentaVinculada(LocalDateTime.now().plusDays(10));

        establecimientoService.bajaEstablecimiento(establecimiento.getId());

        Establecimiento eliminado = establecimientoRepository.findById(establecimiento.getId()).orElseThrow();
        assertThat(eliminado.getCuentaMercadoPagoVigente()).isEmpty();
        assertThat(eliminado.getCuentasMercadoPago()).extracting("fechaHoraBaja").doesNotContainNull();
    }

    @Test
    void soloSeRenuevanCuentasVigentesDeEstablecimientosVigentesPorVencer() {
        LocalDateTime limite = LocalDateTime.now().plusDays(30);

        CuentaMercadoPago porVencer = conCuentaVinculada(LocalDateTime.now().plusDays(10))
                .getCuentaMercadoPagoVigente().orElseThrow();
        CuentaMercadoPago lejosDeVencer = conCuentaVinculada(LocalDateTime.now().plusDays(150))
                .getCuentaMercadoPagoVigente().orElseThrow();

        // Establecimiento dado de baja cuya cuenta quedó vigente (caso defensivo)
        Establecimiento deBaja = conCuentaVinculada(LocalDateTime.now().plusDays(10));
        deBaja.setFechaHoraBaja(LocalDateTime.now());
        CuentaMercadoPago deEstablecimientoDeBaja = establecimientoRepository.save(deBaja)
                .getCuentaMercadoPagoVigente().orElseThrow();

        assertThat(cuentaMercadoPagoRepository.findARenovar(limite))
                .contains(porVencer)
                .doesNotContain(lejosDeVencer, deEstablecimientoDeBaja);
    }
}
