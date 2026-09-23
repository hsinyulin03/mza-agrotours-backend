package com.mza_agrotours.backend.clients.openweather;

import com.mza_agrotours.backend.enums.CondicionClima;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class OpenWeatherCondiciones {
    private static final Logger log = LoggerFactory.getLogger(OpenWeatherCondiciones.class);

    private OpenWeatherCondiciones() {
    }

    static CondicionClima desdeCodigo(int codigo) {
        return switch (codigo / 100) {
            case 2 -> CondicionClima.THUNDERSTORM;
            case 3 -> CondicionClima.SHOWER_RAIN;
            case 5 -> codigo >= 520 ? CondicionClima.SHOWER_RAIN : CondicionClima.RAIN;
            case 6 -> CondicionClima.SNOW;
            case 7 -> CondicionClima.MIST;
            case 8 -> segunNubosidad(codigo);
            default -> {
                log.warn("Codigo de condicion desconocido de OpenWeather: {}", codigo);
                yield CondicionClima.CLEAR_SKY;
            }
        };
    }

    private static CondicionClima segunNubosidad(int codigo) {
        return switch (codigo) {
            case 800 -> CondicionClima.CLEAR_SKY;
            case 801 -> CondicionClima.FEW_CLOUDS;
            case 802 -> CondicionClima.SCATTERED_CLOUDS;
            default -> CondicionClima.BROKEN_CLOUDS;
        };
    }
}