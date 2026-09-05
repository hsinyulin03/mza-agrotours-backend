package com.mza_agrotours.backend.dtos.actividad;

import com.mza_agrotours.backend.entities.Usuario;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.entities.establecimiento.Establecimiento;

import java.util.List;

public record InfoParaReservarDTO(
        // DíaActividad []
        List<DiaActividadReservaDTO> diasActividad,
        // RangoEtario []
        List<RangoEtarioReservaDTO> rangosEtarios,
        // Info del usuario que quiere reservar
        UsuarioPreviewReservaDTO usuario,
        // Actividad
        String nombre,
        String ubicacion,
        String nombreEstablecimiento,
        Integer cupoMaximo,
        Float calificacionPromedio,
        // Parámetros
        Integer diasMinReembolso
) {
    public static InfoParaReservarDTO of(Actividad actividad, Establecimiento establecimiento,
                                          List<DiaActividadReservaDTO> diasActividad,
                                          UsuarioPreviewReservaDTO usuario,
                                          List<RangoEtarioReservaDTO> rangosEtarios,
                                          Integer diasMinReembolso) {
        return new InfoParaReservarDTO(
                diasActividad,
                rangosEtarios,
                usuario,
                actividad.getNombre(),
                establecimiento.getUbicacion(),
                establecimiento.getRazonSocial(),
                actividad.getCuposMax(),
                actividad.getCalificacionPromedio(),
                diasMinReembolso
        );
    }
}
