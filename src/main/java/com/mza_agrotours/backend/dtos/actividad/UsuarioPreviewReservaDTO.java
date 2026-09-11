package com.mza_agrotours.backend.dtos.actividad;

import java.time.LocalDate;

public record UsuarioPreviewReservaDTO(
        String nombreApellido,
        LocalDate fechaNacimiento,
        String tipoIdentificacion,
        String numeroIdentificacion
) {
    public static UsuarioPreviewReservaDTO of(
            String nombreApellido,
            LocalDate fechaNacimiento,
            String tipoId,
            String nroId
    ){
        return new UsuarioPreviewReservaDTO(nombreApellido, fechaNacimiento, tipoId, nroId);
    }
}
