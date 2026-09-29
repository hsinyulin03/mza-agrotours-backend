package com.mza_agrotours.backend.enums;

import lombok.Getter;

@Getter
public enum CondicionClima {
    CLEAR_SKY(0, "clear sky"),
    FEW_CLOUDS(1, "few clouds"),
    SCATTERED_CLOUDS(2, "scattered clouds"),
    BROKEN_CLOUDS(3, "broken clouds"),
    SHOWER_RAIN(4, "shower rain"),
    RAIN(5, "rain"),
    THUNDERSTORM(6, "thunderstorm"),
    SNOW(7, "snow"),
    MIST(8, "mist");

    private final int prioridad;
    private final String nombre;

    private CondicionClima(int prioridad, String nombre) {
        this.prioridad = prioridad;
        this.nombre = nombre;
    }
}
