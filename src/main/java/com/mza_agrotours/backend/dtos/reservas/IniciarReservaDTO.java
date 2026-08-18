package com.mza_agrotours.backend.dtos.reservas;

public record IniciarReservaDTO(
        ConsultarReservaDTO reservaDTO,
        String preferenceId
){

}
