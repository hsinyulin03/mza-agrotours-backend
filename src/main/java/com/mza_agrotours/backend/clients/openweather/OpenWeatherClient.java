package com.mza_agrotours.backend.clients.openweather;

import com.mza_agrotours.backend.clients.openweather.OpenWeatherForecastResponse.Item;
import com.mza_agrotours.backend.dtos.clima.PronosticoSlot;
import com.mza_agrotours.backend.enums.CondicionClima;
import com.mza_agrotours.backend.exceptions.OpenWeatherException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Component
public class OpenWeatherClient {
    private static final String RECURSO_PRONOSTICO = "/forecast";

    private final RestClient openWeatherRestClient;
    private final String apiKey;

    public OpenWeatherClient(RestClient openWeatherRestClient,
                             @Value("${openweather.api-key}") String apiKey) {
        this.openWeatherRestClient = openWeatherRestClient;
        this.apiKey = apiKey;
    }

    public List<PronosticoSlot> obtenerPronostico(Double lat, Double lon) {
        OpenWeatherForecastResponse respuesta;

        try {
            respuesta = this.openWeatherRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(RECURSO_PRONOSTICO)
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("units", "metric")
                            .queryParam("appid", this.apiKey)
                            .build())
                    .retrieve()
                    .body(OpenWeatherForecastResponse.class);
        } catch (RestClientException e) {
            throw new OpenWeatherException(
                    "Fallo la consulta del pronostico para lat=" + lat + " lon=" + lon, e);
        }

        if (respuesta == null || respuesta.list() == null || respuesta.city() == null) {
            throw new OpenWeatherException(
                    "Respuesta sin datos de OpenWeather para lat=" + lat + " lon=" + lon);
        }

        ZoneOffset zonaLocal = ZoneOffset.ofTotalSeconds(respuesta.city().timezone());
        return respuesta.list().stream()
                .map(item -> this.aSlot(item, zonaLocal))
                .toList();
    }

    private PronosticoSlot aSlot(Item item, ZoneOffset zonaLocal) {
        LocalDate fechaLocal = Instant.ofEpochSecond(item.dt()).atOffset(zonaLocal).toLocalDate();
        Double temperatura = item.main().temp();

        return new PronosticoSlot(
                fechaLocal,
                temperatura,
                item.main().temperaturaMax() != null ? item.main().temperaturaMax() : temperatura,
                item.main().temperaturaMin() != null ? item.main().temperaturaMin() : temperatura,
                item.pop() != null ? item.pop() * 100 : 0.0,
                this.condicionDe(item));
    }

    private CondicionClima condicionDe(Item item) {
        if (item.weather() == null || item.weather().isEmpty()) {
            return CondicionClima.CLEAR_SKY;
        }
        return OpenWeatherCondiciones.desdeCodigo(item.weather().get(0).id());
    }
}
