package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.enums.MetodoPago;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EstrategiaPagoFactory {
    private final Map<MetodoPago, EstrategiaPago> estrategias;
    public EstrategiaPagoFactory(List<EstrategiaPago> implementaciones) {
        this.estrategias = implementaciones.stream()
                .collect(Collectors.toMap(EstrategiaPago::getMetodo, e -> e));
    }
    /**
     * @throws IllegalStateException si no hay una estrategia registrada para el método de pago
     */
    public EstrategiaPago get(MetodoPago metodo) {
        EstrategiaPago estrategia = estrategias.get(metodo);
        if (estrategia == null)
            throw new IllegalStateException("No hay estrategia de pago para el método " + metodo);
        return estrategia;
    }
}
