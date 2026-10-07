package com.mza_agrotours.backend.services.pago;

import com.mza_agrotours.backend.clients.mercadopago.MercadoPagoOAuthClient;
import com.mza_agrotours.backend.clients.mercadopago.MercadoPagoOAuthTokenResponse;
import com.mza_agrotours.backend.dtos.establecimiento.DTOCuentaMercadoPagoEstado;
import com.mza_agrotours.backend.entities.establecimiento.CuentaMercadoPago;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;
import com.mza_agrotours.backend.entities.productor.Productor;
import com.mza_agrotours.backend.exceptions.EstablecimientoNotFoundException;
import com.mza_agrotours.backend.exceptions.pago.MercadoPagoOAuthException;
import com.mza_agrotours.backend.repositories.CuentaMercadoPagoRepository;
import com.mza_agrotours.backend.repositories.EstablecimientoRepository;
import com.mza_agrotours.backend.security.cifrado.Cifrador;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Vinculación por OAuth de la cuenta de Mercado Pago de un establecimiento (vendedor)
 * con la aplicación de Agrotours (marketplace), y renovación de sus tokens.
 * <p></p>
 * El {@code state} de OAuth no se guarda: es un valor cifrado y autenticado con {@link Cifrador}
 * que contiene establecimiento, productor que inició la vinculación y vencimiento. Nadie puede
 * fabricar o modificar uno válido sin la clave, y el {@code code} de MP es de un solo uso.
 */
@Slf4j
@Service
public class CuentaMercadoPagoService {

    private static final Duration VALIDEZ_STATE = Duration.ofMinutes(10);   // Igual a la validez del code de MP
    private static final Duration ANTICIPACION_RENOVACION = Duration.ofDays(30);
    private static final String SEPARADOR_STATE = "|";

    public enum ResultadoVinculacion { OK, CANCELADA, ERROR }

    public record ResultadoCallback(ResultadoVinculacion resultado, UUID establecimientoId) { }

    private record DatosState(UUID establecimientoId, UUID productorId, Instant vencimiento) { }

    private final EstablecimientoRepository establecimientoRepository;
    private final CuentaMercadoPagoRepository cuentaMercadoPagoRepository;
    private final MercadoPagoOAuthClient oAuthClient;
    private final Cifrador cifrador;

    public CuentaMercadoPagoService(EstablecimientoRepository establecimientoRepository,
                                    CuentaMercadoPagoRepository cuentaMercadoPagoRepository,
                                    MercadoPagoOAuthClient oAuthClient,
                                    Cifrador cifrador) {
        this.establecimientoRepository = establecimientoRepository;
        this.cuentaMercadoPagoRepository = cuentaMercadoPagoRepository;
        this.oAuthClient = oAuthClient;
        this.cifrador = cifrador;
    }

    /**
     * Genera la URL de Mercado Pago a la que se redirige al productor líder para autorizar la vinculación.
     * La autorización (que quien llama sea el titular) se valida en el controller.
     */
    @Transactional(readOnly = true)
    public String generarUrlVinculacion(UUID establecimientoId) {
        Establecimiento establecimiento = getEstablecimientoVigente(establecimientoId);

        String state = cifrador.cifrar(String.join(SEPARADOR_STATE,
                establecimiento.getId().toString(),
                establecimiento.getTitular().getId().toString(),
                String.valueOf(Instant.now().plus(VALIDEZ_STATE).getEpochSecond())));

        return oAuthClient.construirUrlAutorizacion(state);
    }

    /**
     * Procesa la vuelta de Mercado Pago al callback de OAuth. Nunca lanza excepciones:
     * el resultado se usa para redirigir al front, y los motivos de error quedan en el log.
     *
     * @param code  código de autorización; null si el productor no autorizó
     * @param state state que generó {@link #generarUrlVinculacion}
     */
    @Transactional
    public ResultadoCallback procesarCallback(String code, String state) {
        DatosState datos;
        try {
            datos = leerState(state);
        } catch (RuntimeException e) {
            log.warn("Callback de OAuth de MP con state inválido: {}", e.getMessage());
            return new ResultadoCallback(ResultadoVinculacion.ERROR, null);
        }

        UUID establecimientoId = datos.establecimientoId();

        if (code == null || code.isBlank()) {
            log.info("El productor no autorizó la vinculación de MP para el establecimiento {}", establecimientoId);
            return new ResultadoCallback(ResultadoVinculacion.CANCELADA, establecimientoId);
        }

        try {
            Establecimiento establecimiento = getEstablecimientoVigente(establecimientoId);
            Productor titular = establecimiento.getTitular();

            // Quien inició la vinculación tiene que seguir siendo el productor líder
            if (titular == null || titular.getFechaHoraBaja() != null || !titular.getId().equals(datos.productorId())) {
                log.warn("Vinculación de MP rechazada: el productor {} ya no es titular del establecimiento {}",
                        datos.productorId(), establecimientoId);
                return new ResultadoCallback(ResultadoVinculacion.ERROR, establecimientoId);
            }

            MercadoPagoOAuthTokenResponse tokens = oAuthClient.canjearCodigo(code);
            LocalDateTime ahora = LocalDateTime.now();

            CuentaMercadoPago cuenta = new CuentaMercadoPago();
            cuenta.setVinculadaPor(titular);
            aplicarTokens(cuenta, tokens, ahora);

            establecimiento.vincularCuentaMercadoPago(cuenta, ahora);
            establecimientoRepository.save(establecimiento);

            log.info("Establecimiento {} vinculó la cuenta de MP {}", establecimientoId, tokens.userId());
            return new ResultadoCallback(ResultadoVinculacion.OK, establecimientoId);
        } catch (MercadoPagoOAuthException | EstablecimientoNotFoundException e) {
            log.warn("Falló la vinculación de MP del establecimiento {}: {}", establecimientoId, e.getMessage());
            return new ResultadoCallback(ResultadoVinculacion.ERROR, establecimientoId);
        }
    }

    @Transactional(readOnly = true)
    public DTOCuentaMercadoPagoEstado obtenerEstado(UUID establecimientoId) {
        return getEstablecimientoVigente(establecimientoId).getCuentaMercadoPagoVigente()
                .map(c -> new DTOCuentaMercadoPagoEstado(true, c.getFechaHoraAlta(), c.getFechaHoraExpiracionToken()))
                .orElseGet(DTOCuentaMercadoPagoEstado::sinVincular);
    }

    /**
     * IDs de las cuentas vigentes cuyo token vence dentro del período de anticipación.
     */
    @Transactional(readOnly = true)
    public List<UUID> getCuentasARenovar() {
        return cuentaMercadoPagoRepository
                .findARenovar(LocalDateTime.now().plus(ANTICIPACION_RENOVACION))
                .stream()
                .map(CuentaMercadoPago::getId)
                .toList();
    }

    /**
     * Renueva los tokens de una cuenta con su refresh token. Cada cuenta se renueva en su propia transacción.
     */
    @Transactional
    public void renovarTokens(UUID cuentaId) {
        CuentaMercadoPago cuenta = cuentaMercadoPagoRepository.findById(cuentaId)
                .orElseThrow(() -> new IllegalStateException("No existe la cuenta de MP " + cuentaId));
        if (!cuenta.isVigente() || cuenta.getEstablecimiento().getFechaHoraBaja() != null) return;

        MercadoPagoOAuthTokenResponse tokens = oAuthClient.renovarToken(cuenta.getRefreshToken());
        LocalDateTime ahora = LocalDateTime.now();

        aplicarTokens(cuenta, tokens, ahora);
        cuenta.setFechaHoraUltimaRenovacion(ahora);
        cuentaMercadoPagoRepository.save(cuenta);
    }

    private void aplicarTokens(CuentaMercadoPago cuenta, MercadoPagoOAuthTokenResponse tokens, LocalDateTime ahora) {
        cuenta.setMpUserId(tokens.userId());
        cuenta.setAccessToken(tokens.accessToken());
        cuenta.setRefreshToken(tokens.refreshToken());
        if (tokens.publicKey() != null) cuenta.setPublicKey(tokens.publicKey());
        cuenta.setFechaHoraExpiracionToken(ahora.plusSeconds(tokens.expiresIn()));
    }

    private DatosState leerState(String state) {
        if (state == null || state.isBlank()) throw new IllegalArgumentException("state vacío");

        String[] partes = cifrador.descifrar(state).split("\\" + SEPARADOR_STATE);
        if (partes.length != 3) throw new IllegalArgumentException("state con formato inválido");

        DatosState datos = new DatosState(
                UUID.fromString(partes[0]),
                UUID.fromString(partes[1]),
                Instant.ofEpochSecond(Long.parseLong(partes[2])));

        if (Instant.now().isAfter(datos.vencimiento())) throw new IllegalArgumentException("state vencido");
        return datos;
    }

    private Establecimiento getEstablecimientoVigente(UUID establecimientoId) {
        return establecimientoRepository.findById(establecimientoId)
                .filter(e -> e.getFechaHoraBaja() == null)
                .orElseThrow(EstablecimientoNotFoundException::new);
    }
}