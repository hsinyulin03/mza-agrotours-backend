package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService.ResultadoCallback;
import com.mza_agrotours.backend.services.pago.CuentaMercadoPagoService.ResultadoVinculacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Callback de OAuth al que Mercado Pago redirige el navegador del productor después de autorizar.
 * Es público (llega sin token de Firebase): lo protege la validación del state en el service.
 * La ruta tiene que coincidir exactamente con la "URL de redireccionamiento" de la aplicación en MP.
 */
@Slf4j
@RestController
@RequestMapping("/mercadopago/oauth")
public class MercadoPagoOAuthCallbackController {

    private final CuentaMercadoPagoService cuentaMercadoPagoService;
    private final String frontRetornoUrl;

    public MercadoPagoOAuthCallbackController(CuentaMercadoPagoService cuentaMercadoPagoService,
                                              @Value("${mercadopago.oauth.front-retorno-url}") String frontRetornoUrl) {
        this.cuentaMercadoPagoService = cuentaMercadoPagoService;
        this.frontRetornoUrl = frontRetornoUrl;
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam(required = false) String code,
                                         @RequestParam(required = false) String state) {
        ResultadoCallback resultado;
        try {
            resultado = cuentaMercadoPagoService.procesarCallback(code, state);
        } catch (RuntimeException e) {
            log.error("Error inesperado procesando el callback de OAuth de MP", e);
            resultado = new ResultadoCallback(ResultadoVinculacion.ERROR, null);
        }

        UriComponentsBuilder destino = UriComponentsBuilder.fromUriString(frontRetornoUrl)
                .queryParam("resultado", resultado.resultado().name().toLowerCase());
        if (resultado.establecimientoId() != null)
            destino.queryParam("establecimientoId", resultado.establecimientoId());

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(destino.encode().toUriString()))
                .build();
    }
}