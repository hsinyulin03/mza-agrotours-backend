package com.mza_agrotours.backend.services.pago;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPElementsResourcesPage;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import com.mercadopago.resources.merchantorder.MerchantOrderPayment;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentRefund;
import com.mercadopago.resources.preference.Preference;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaPagoDTO;
import com.mza_agrotours.backend.dtos.pago.ResultadoConsultaReembolso;
import com.mza_agrotours.backend.dtos.pago.ResultadoReembolsoDTO;
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
import com.mza_agrotours.backend.exceptions.pago.PasarelaPagoException;
import com.mza_agrotours.backend.repositories.pago.PagoRepository;
import com.mza_agrotours.backend.services.ParametrosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PagoMPStrategy implements EstrategiaPago{
    private static final Logger log = LoggerFactory.getLogger(PagoMPStrategy.class);

    private final PagoRepository pagoRepository;
    private final ParametrosService parametrosService;
    private final CuentaMercadoPagoService cuentaMercadoPagoService;
    private final ConciliadorPagoMP conciliadorPagoMP;

    public PagoMPStrategy(PagoRepository pagoRepository, ParametrosService parametrosService, CuentaMercadoPagoService cuentaMercadoPagoService, ConciliadorPagoMP conciliadorPagoMP) {
        this.pagoRepository = pagoRepository;
        this.parametrosService = parametrosService;
        this.cuentaMercadoPagoService = cuentaMercadoPagoService;
        this.conciliadorPagoMP = conciliadorPagoMP;
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

    /**
     * Workaround al todavía no tener notificaciones webhook de Mercado Pago: busca las merchant orders de la
     * preference del pago y concilia con la reserva ({@link ConciliadorPagoMP}) el primer pago aprobado que encuentre.
     */
    @Override
    public ResultadoConsultaPagoDTO consultarPago(Reserva reserva) {
        Pago pago = reserva.getPago();
        String preferenceId = pago.getIdCheckoutExterno();
        try {
            // La preference, sus merchant orders y sus pagos están en la cuenta del vendedor que la creó
            MPRequestOptions opcionesMP = opcionesDe(pago);

            // Creamos el tipo de búsqueda que queremos hacer: por preferenceId
            MPSearchRequest searchRequest = MPSearchRequest.builder()
                    .filters(Map.of("preference_id", preferenceId))
                    .limit(10)  // Debería haber 1 MO por preference, puede haber más si paga lo mismo varias veces
                    .offset(0)  // Buscamos el primero, sin offest
                    .build();

            MPElementsResourcesPage<MerchantOrder> resultado = new MerchantOrderClient().search(searchRequest, opcionesMP);
            if (resultado.getElements() == null) return ResultadoConsultaPagoDTO.noAprobado(); // Sin merchant order no hay pagos

            List<Long> aprobados = resultado.getElements().stream()
                    .filter(mo -> mo.getPayments() != null)
                    .flatMap(mo -> mo.getPayments().stream())
                    .filter(p -> "approved".equals(p.getStatus()))
                    .map(MerchantOrderPayment::getId)
                    .toList();
            if (aprobados.isEmpty()) return ResultadoConsultaPagoDTO.noAprobado();

            // NOTE: gap conocido, los pagos aprobados extra de una misma preference no se reembolsan automáticamente.
            //  Se ignoran: solo se concilia (o reembolsa, si no concilia) el primero
            if (aprobados.size() > 1)
                log.warn("La reserva {} tiene {} pagos aprobados en MP {}: se concilia el primero, el resto requiere reembolso manual",
                        reserva.getId(), aprobados.size(), aprobados);

            // Conciliamos con el detalle del pago (monto, vendedor y comisiones) antes de darlo por aprobado
            Payment payment = new PaymentClient().get(aprobados.get(0), opcionesMP);
            conciliadorPagoMP.conciliar(reserva, payment);

            return new ResultadoConsultaPagoDTO(true, payment.getId().toString());
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
            new PreferenceClient().update(pago.getIdCheckoutExterno(), preferenceRequest, opcionesDe(pago));
        } catch (MPApiException e) {
            throw new PasarelaPagoException("Error de la API de MP expirando la preference: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            throw new PasarelaPagoException("Error de red/SDK expirando la preference", e);
        }
    }

    /**
     * Pide el reembolso total del payment aprobado, con el token del vendedor que lo cobró. Usa una idempotency
     * key por pago para que un doble pedido no genere dos reembolsos. Un error 4xx de MP es un rechazo; un 5xx
     * o un error de red no garantizan que el reembolso no se haya hecho, así que se lanzan.
     */
    @Override
    public ResultadoReembolsoDTO reembolsar(Pago pago) {
        if (pago.getIdTransaccionExterna() == null) {
            log.warn("El pago {} no tiene ID de transacción de MP, no se puede reembolsar", pago.getId());
            return ResultadoReembolsoDTO.rechazado();
        }

        MPRequestOptions options = MPRequestOptions.builder()
                .accessToken(opcionesDe(pago).getAccessToken())
                .customHeaders(Map.of("X-Idempotency-Key", "reembolso-pago-" + pago.getId()))
                .build();
        try {
            PaymentRefund refund = new PaymentRefundClient()
                    .refund(Long.valueOf(pago.getIdTransaccionExterna()), options);
            return new ResultadoReembolsoDTO(true, String.valueOf(refund.getId()));
        } catch (MPApiException e) {
            if (e.getStatusCode() >= 500)
                throw new PasarelaPagoException("Error interno de MP al reembolsar, no se sabe si se realizó: " + e.getApiResponse().getContent(), e);
            log.warn("MP rechazó el reembolso del pago {}: {}", pago.getId(), e.getApiResponse().getContent());
            return ResultadoReembolsoDTO.rechazado();
        } catch (MPException e) {
            throw new PasarelaPagoException("Error de red/SDK al reembolsar, no se sabe si se realizó", e);
        }
    }

    /**
     * Lista los refunds del payment. Como solo se hacen reembolsos totales, hay a lo sumo uno.
     */
    @Override
    public Optional<String> buscarReembolso(Pago pago) {
        try {
            List<PaymentRefund> refunds = new PaymentRefundClient()
                    .list(Long.valueOf(pago.getIdTransaccionExterna()), opcionesDe(pago))
                    .getResults();
            if (refunds == null || refunds.isEmpty()) return Optional.empty();
            return Optional.of(String.valueOf(refunds.get(0).getId()));
        } catch (MPApiException e) {
            throw new PasarelaPagoException("Error de la API de MP listando los refunds: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            throw new PasarelaPagoException("Error de red/SDK listando los refunds", e);
        }
    }

    /**
     * Workaround al todavía no tener notificaciones webhook de Mercado Pago: consulta el refund por su ID.
     * "approved" es aprobado, "rejected" y "cancelled" son rechazados, y cualquier otro ("in_process",
     * "authorized") sigue en proceso.
     */
    @Override
    public ResultadoConsultaReembolso consultarReembolso(Pago pago, String idReembolsoExterno) {
        try {
            PaymentRefund refund = new PaymentRefundClient()
                    .get(Long.valueOf(pago.getIdTransaccionExterna()), Long.valueOf(idReembolsoExterno), opcionesDe(pago));

            String status = refund.getStatus();
            if ("approved".equals(status)) return ResultadoConsultaReembolso.APROBADO;
            if ("rejected".equals(status) || "cancelled".equals(status)) return ResultadoConsultaReembolso.RECHAZADO;
            return ResultadoConsultaReembolso.EN_PROCESO;
        } catch (MPApiException e) {
            throw new PasarelaPagoException("Error de la API de MP consultando el refund: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            throw new PasarelaPagoException("Error de red/SDK consultando el refund", e);
        }
    }

    /**
     * Opciones de las llamadas a MP sobre la preference o el payment del pago: usan el token de la cuenta del
     * vendedor que creó la preference (o el global de Agrotours si el pago es anterior al split).
     */
    private MPRequestOptions opcionesDe(Pago pago) {
        return cuentaMercadoPagoService.opcionesDe(pago.getCuentaMercadoPago());
    }
}
