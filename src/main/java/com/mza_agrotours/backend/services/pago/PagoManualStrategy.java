package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaReembolso;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.exceptions.pago.EstadoPagoNotFoundException;
import com.mza_agrotours.backend.repositories.pago.PagoRepository;
import com.mza_agrotours.backend.services.ParametrosService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

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
    public PagoStrategyDTO procesarPago(Reserva reserva) {
        LocalDateTime ahora = LocalDateTime.now();
        Pago pago = new Pago();

        pago.setMetodoPago(MetodoPago.MANUAL);
        pago.setFechaHoraPago(ahora);
        pago.setMontoTotal(reserva.getTotalReserva());

        EstadoPago estadoAprobado = pagoRepository.findEstadoPagoByEstadoPagoNombre(EstadoPagoNombre.APROBADO)
                .orElseThrow(() -> new EstadoPagoNotFoundException(EstadoPagoNombre.APROBADO));

        pago.cambiarEstado(estadoAprobado, ahora);

        reserva.setSubTotalComisionTransaccion(BigDecimal.valueOf(0));
        reserva.setSubTotalComisionPropia(
                reserva.getTotalReserva()
                        .multiply(parametrosService.getInstance().getPorcentajeComision())
                        .setScale(2, RoundingMode.HALF_UP)
        );
        reserva.setSubTotalProductor(
                reserva.getTotalReserva().subtract(
                        reserva.getSubTotalComisionPropia()
                )
        );

        reserva.setPago(pago);

        return new PagoStrategyDTO(pago, null);
    }

    // El pago manual se aprueba al procesarlo, no hay pasarela que consultar
    @Override
    public ResultadoConsultaPagoDTO consultarPago(Reserva reserva) {
        return new ResultadoConsultaPagoDTO(true, null);
    }

    @Override
    public void cancelarCheckout(Pago pago, LocalDateTime ahora) {
        // No hay sesión de cobro que invalidar
    }

    // Igual que al pagar, se hace de cuenta que el reembolso se realizó inmediatamente
    @Override
    public ResultadoReembolsoDTO reembolsar(Pago pago) {
        return new ResultadoReembolsoDTO(true, null);
    }

    // El reembolso manual se completa al pedirlo, nunca queda sin confirmar
    @Override
    public Optional<String> buscarReembolso(Pago pago) {
        return Optional.empty();
    }

    // El reembolso manual se da por realizado al pedirlo, no hay pasarela que consultar
    @Override
    public ResultadoConsultaReembolso consultarReembolso(Pago pago, String idReembolsoExterno) {
        return ResultadoConsultaReembolso.APROBADO;
    }
}
