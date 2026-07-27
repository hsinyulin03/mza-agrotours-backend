package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoPagoNombre;
import com.mza_agrotours.backend.entities.pago.MetodoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.pago.EstadoPagoNotFoundException;
import com.mza_agrotours.backend.repositories.pago.PagoRepository;
import com.mza_agrotours.backend.services.ParametrosService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class PagoManualStrategy implements EstrategiaPago{

    private final PagoRepository pagoRepository;
    private final ParametrosService parametrosService;

    public PagoManualStrategy(PagoRepository pagoRepository, ParametrosService parametrosService) {
        this.pagoRepository = pagoRepository;
        this.parametrosService = parametrosService;
    }

    @Override
    public MetodoPago getMetodo() {
        return MetodoPago.MANUAL;
    }

    @Override
    public Pago procesarPago(Reserva reserva) {
        LocalDateTime ahora = LocalDateTime.now();
        Pago pago = new Pago();

        pago.setMetodoPago(MetodoPago.MANUAL);
        pago.setIdPagoExterno("MANUAL-"+UUID.randomUUID());
        pago.setFechaHoraPago(ahora);
        pago.setMontoTotal(reserva.getTotalReserva());

        EstadoPago estadoAprobado = pagoRepository.findEstadoPagoByEstadoPagoNombre(EstadoPagoNombre.APROBADO)
                .orElseThrow(() -> new EstadoPagoNotFoundException(EstadoPagoNombre.APROBADO));

        pago.cambiarEstado(estadoAprobado, ahora);

        reserva.setSubtotalComisionTransaccion(BigDecimal.valueOf(0));
        reserva.setSubTotalComisionPropia(
                reserva.getTotalReserva().multiply(
                        BigDecimal.valueOf(parametrosService.getInstance().getPorcentajeComision())
                )
        );
        reserva.setSubtotalProductor(
                reserva.getTotalReserva().subtract(
                        reserva.getSubTotalComisionPropia()
                )
        );

        reserva.setPago(pago);

        return pago;
    }
}
