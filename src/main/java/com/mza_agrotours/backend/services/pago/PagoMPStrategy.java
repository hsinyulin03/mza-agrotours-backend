package com.mza_agrotours.backend.services.pago;

import com.google.api.client.util.Value;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPElementsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import com.mercadopago.resources.payment.PaymentRefund;
import com.mercadopago.resources.preference.Preference;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
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
import com.mza_agrotours.backend.exceptions.pago.PasarelaPagoException;
import com.mza_agrotours.backend.repositories.pago.PagoRepository;
import com.mza_agrotours.backend.services.ParametrosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PagoMPStrategy implements EstrategiaPago{
    private static final Logger log = LoggerFactory.getLogger(PagoMPStrategy.class);

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
            pago.setIdCheckoutExterno(preference.getId());
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

    /**
     * Workaround al todavía no tener notificaciones webhook de Mercado Pago: busca las merchant orders de la
     * preference del pago y devuelve el primer pago aprobado que encuentre.
     */
    @Override
    public ResultadoConsultaPagoDTO consultarPago(Pago pago) {
        String preferenceId = pago.getIdCheckoutExterno();
        try {
            // Creamos el tipo de búsqueda que queremos hacer: por preferenceId
            MPSearchRequest searchRequest = MPSearchRequest.builder()
                    .filters(Map.of("preference_id", preferenceId))
                    .limit(10)  // Debería haber 1 MO por preference, puede haber más si paga lo mismo varias veces
                    .offset(0)  // Buscamos el primero, sin offest
                    .build();

            MPElementsResourcesPage<MerchantOrder> resultado = new MerchantOrderClient().search(searchRequest);
            if (resultado.getElements() == null) return ResultadoConsultaPagoDTO.noAprobado(); // Sin merchant order no hay pagos

            // Buscar pago aprobado TODO - Buscamos el primer pago pero nunca comparamos que sea por el total. No veo por qué NO lo sería, pero es un punto débil
            return resultado.getElements().stream()
                    .filter(mo -> mo.getPayments() != null)
                    .flatMap(mo -> mo.getPayments().stream())
                    .filter(p -> "approved".equals(p.getStatus()))
                    .findFirst()
                    .map(p -> new ResultadoConsultaPagoDTO(true, String.valueOf(p.getId())))
                    .orElseGet(ResultadoConsultaPagoDTO::noAprobado);
        } catch (MPApiException e) {
            throw new PasarelaPagoException("Error de la API de MP consultando merchant orders: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            throw new PasarelaPagoException("Error de red/SDK consultando merchant orders", e);
        }
    }

    /**
     * Expira la preference del pago, seteando su fecha de expiración al momento indicado.
     */
    @Override
    public void cancelarCheckout(Pago pago, LocalDateTime ahora) {
        if (pago.getIdCheckoutExterno() == null) return;
        try {
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .expirationDateTo(ahora.atZone(ZoneId.systemDefault()).toOffsetDateTime())
                    .build();
            new PreferenceClient().update(pago.getIdCheckoutExterno(), preferenceRequest);
        } catch (MPApiException e) {
            throw new PasarelaPagoException("Error de la API de MP expirando la preference: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            throw new PasarelaPagoException("Error de red/SDK expirando la preference", e);
        }
    }

    /**
     * Pide el reembolso total del payment aprobado. Usa una idempotency key por pago para que un doble
     * pedido no genere dos reembolsos.
     */
    @Override
    public ResultadoReembolsoDTO reembolsar(Pago pago) {
        if (pago.getIdTransaccionExterna() == null) {
            log.warn("El pago {} no tiene ID de transacción de MP, no se puede reembolsar", pago.getId());
            return ResultadoReembolsoDTO.rechazado();
        }

        MPRequestOptions options = MPRequestOptions.builder()
                .customHeaders(Map.of("X-Idempotency-Key", "reembolso-pago-" + pago.getId()))
                .build();
        try {
            PaymentRefund refund = new PaymentRefundClient()
                    .refund(Long.valueOf(pago.getIdTransaccionExterna()), options);
            return new ResultadoReembolsoDTO(true, String.valueOf(refund.getId()));
        } catch (MPApiException e) {
            log.warn("Error MP al reembolsar el pago {}: {}", pago.getId(), e.getApiResponse().getContent());
            return ResultadoReembolsoDTO.rechazado();
        } catch (MPException e) {
            log.warn("Error de comunicación con MP al reembolsar el pago {}", pago.getId(), e);
            return ResultadoReembolsoDTO.rechazado();
        }
    }
}
