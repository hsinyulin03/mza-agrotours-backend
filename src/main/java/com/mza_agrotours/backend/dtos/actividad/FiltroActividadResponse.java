package com.mza_agrotours.backend.dtos.actividad;

import java.util.List;

public record FiltroActividadResponse(
        List<DTOFiltro> filtroDepartamentos,
        List<DTOFiltro> filtroCultivos,
        List<FiltroRangoPrecio> filtroPrecios,
        List<DTOFiltro> filtroCalificaciones) {
}
