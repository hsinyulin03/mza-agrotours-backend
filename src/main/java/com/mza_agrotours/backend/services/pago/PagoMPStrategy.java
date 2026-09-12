package com.mza_agrotours.backend.services.pago;

import com.google.api.client.util.Value;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import com.mza_agrotours.backend.dtos.reservas.PagoStrategyDTO;
import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.Visitante;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.actividad.ActividadDia;
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

    public PagoMPStrategy(PagoRepository pagoRepository, ParametrosService parametrosService) {
        this.pagoRepository = pagoRepository;
        this.parametrosService = parametrosService;
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
            PreferenceRequest preferenceRequest = PreferenceRequest.builder() // TODO notificaciones, back url y fee
                    .items(items)
                    .payer(payer)
                    .paymentMethods(paymentMethods)
                    .statementDescriptor("MDZ_AGROTOURS")
                    .expires(true)
                    .expirationDateFrom(OffsetDateTime.now())
                    .expirationDateTo(reserva.getFechaHoraExpiracion().atZone(ZoneId.systemDefault()).toOffsetDateTime())
                    .externalReference(reserva.getId().toString())
                    .build();

            // Creamos el cliente y enviamos la PreferenceReq a que sea aceptada por MP
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Ahora creamos el pago en estado PENDIENTE
            LocalDateTime ahora = LocalDateTime.now();
            Pago pago = new Pago();

            pago.setMetodoPago(MetodoPago.MERCADO_PAGO);
            pago.setIdPagoExterno(preference.getId());
            pago.setFechaHoraPago(ahora);
            pago.setMontoTotal(reserva.getTotalReserva());

            EstadoPago estadoPendiente = pagoRepository.findEstadoPagoByEstadoPagoNombre(EstadoPagoNombre.PENDIENTE)
                    .orElseThrow(() -> new EstadoPagoNotFoundException(EstadoPagoNombre.PENDIENTE));

            pago.cambiarEstado(estadoPendiente, ahora);

            // Info del pago
            reserva.setSubTotalComisionTransaccion(BigDecimal.valueOf(0)); // TODO fee nuestra y del marketplace
            reserva.setSubTotalComisionPropia(
                    reserva.getTotalReserva().multiply(
                            BigDecimal.valueOf(parametrosService.getInstance().getPorcentajeComision())
                    )
            );
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
