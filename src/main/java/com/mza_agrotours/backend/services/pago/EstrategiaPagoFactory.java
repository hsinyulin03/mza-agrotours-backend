package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.entities.pago.MetodoPago;
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
    public EstrategiaPago get(MetodoPago metodo) {
        return estrategias.get(metodo);
    }
}
