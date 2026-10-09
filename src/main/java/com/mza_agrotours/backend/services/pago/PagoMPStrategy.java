package com.mza_agrotours.backend.services.pago;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.pago.EstadoPago;
import com.mza_agrotours.backend.entities.pago.Pago;
import com.mza_agrotours.backend.entities.reservas.Reserva;
import com.mza_agrotours.backend.enums.EstadoPagoNombre;
import com.mza_agrotours.backend.enums.MetodoPago;
import com.mza_agrotours.backend.exceptions.pago.EstadoPagoNotFoundException;
import com.mza_agrotours.backend.repositories.pago.PagoRepository;
import com.mza_agrotours.backend.services.ParametrosService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class PagoMPStrategy implements EstrategiaPago{

    private final PagoRepository pagoRepository;
    private final ParametrosService parametrosService;
    private final CuentaMercadoPagoService cuentaMercadoPagoService;

    public PagoMPStrategy(PagoRepository pagoRepository, ParametrosService parametrosService, CuentaMercadoPagoService cuentaMercadoPagoService) {
        this.pagoRepository = pagoRepository;
        this.parametrosService = parametrosService;
        this.cuentaMercadoPagoService = cuentaMercadoPagoService;
    }

    @Override
    public MetodoPago getMetodo() {
        return MetodoPago.MERCADO_PAGO;
    }

    @Override
    public PagoStrategyDTO procesarPago(Reserva reserva){
        Visitante visitante = reserva.getVisitante();
        Usuario usuario = visitante.getUsuario();
        Actividad actividad = reserva.getActividad();
        ActividadDia actividadDia = reserva.getActividadDia();

        // El cobro se acredita en la cuenta del establecimiento; fuera del try para que llegue tal cual al handler
        CuentaMercadoPago cuentaVendedor = cuentaMercadoPagoService.getCuentaParaCobrar(actividad.getEstablecimiento());

        // Comisión de Agrotours, que MP descuenta del cobro al vendedor como marketplace_fee
        BigDecimal comisionPropia = reserva.getTotalReserva()
                .multiply(parametrosService.getInstance().getPorcentajeComision())
                .setScale(2, RoundingMode.HALF_UP);

        try {
            // Creamos el item de la Preference Request
            PreferenceItemRequest itemRequest =
                    PreferenceItemRequest.builder()
                            .id(actividadDia.getId().toString())
                            .title(actividad.getNombre())
                            .description(actividad.getDescripcion())
                            .categoryId("tickets")
                            .quantity(1)
                            .currencyId("ARS")
                            .unitPrice(reserva.getTotalReserva())
                            .build();
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // Creamos la información del Payer
            IdentificationRequest identification = IdentificationRequest.builder()
                    .type(usuario.getTipoIdentificacion().getNombre().name())
                    .number(usuario.getIdentificacion())
                    .build();

            // NOTE: Se excluyen atributos de payer que no se pueden obtener por cómo es nuestro sistema
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                    .name(usuario.getNombre())
                    .email(usuario.getEmail())
                    .identification(identification)
                    .build();

            // Creamos la información de los Payment Types que vamos a excluir
            List<PreferencePaymentTypeRequest> excludedPaymentTypes = new ArrayList<>();
            excludedPaymentTypes.add(PreferencePaymentTypeRequest.builder().id("ticket").build());

            PreferencePaymentMethodsRequest paymentMethods = PreferencePaymentMethodsRequest.builder()
                    .excludedPaymentTypes(excludedPaymentTypes)
                    .installments(12)
                    .build();

            // Creamos la preference Request con Items, Payer, Métodos de Pago, info del marketplace y un par de datos nuevos
            PreferenceRequest preferenceRequest = PreferenceRequest.builder() // TODO back url
                    .items(items)
                    .marketplaceFee(comisionPropia)
                    .payer(payer)
                    .paymentMethods(paymentMethods)
                    .statementDescriptor("MDZ_AGROTOURS")
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now())
                    .expirationDateTo(reserva.getFechaHoraExpiracion().atZone(ZoneId.systemDefault()).toOffsetDateTime())
                    .externalReference(reserva.getId().toString())
                    .build();

            // Creamos la preference con el token del vendedor, así el pago queda en su cuenta
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest, cuentaMercadoPagoService.opcionesDe(cuentaVendedor));

            // Ahora creamos el pago en estado PENDIENTE
            LocalDateTime ahora = LocalDateTime.now();
            Pago pago = new Pago();

            pago.setMetodoPago(MetodoPago.MERCADO_PAGO);
            pago.setIdCheckoutExterno(preference.getId());   // idTransaccionExterna se completa al conciliar el pago aprobado
            pago.setFechaHoraPago(ahora);
            pago.setMontoTotal(reserva.getTotalReserva());
            pago.setCuentaMercadoPago(cuentaVendedor);

            EstadoPago estadoPendiente = pagoRepository.findEstadoPagoByEstadoPagoNombre(EstadoPagoNombre.PENDIENTE)
                    .orElseThrow(() -> new EstadoPagoNotFoundException(EstadoPagoNombre.PENDIENTE));

            pago.cambiarEstado(estadoPendiente, ahora);

            // Info del pago
            reserva.setSubTotalComisionTransaccion(BigDecimal.valueOf(0)); // TODO comisión de MP: se conoce recién con el pago aprobado (fee_details)
            reserva.setSubTotalComisionPropia(comisionPropia);
            reserva.setSubTotalProductor(
                    reserva.getTotalReserva().subtract(
                            reserva.getSubTotalComisionPropia()
                    )
            );

            reserva.setPago(pago);

            // Se devuelve el pago preference ID
            return new PagoStrategyDTO(pago, preference.getId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
