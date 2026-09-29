package com.mza_agrotours.backend.clients.openweather;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenWeatherForecastResponse(List<Item> list, City city) {

    public record Item(long dt, Main main, List<Weather> weather, Double pop) {
    }

    public record Main(
            Double temp,
            @JsonProperty("temp_max") Double temperaturaMax,
            @JsonProperty("temp_min") Double temperaturaMin) {
    }

    public record Weather(int id) {
    }

    public record City(int timezone) {
    }
}
