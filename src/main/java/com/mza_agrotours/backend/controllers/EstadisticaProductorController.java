package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.estadisticasproductor.ActividadPerformanceDTO;
import com.mza_agrotours.backend.dtos.estadisticasproductor.EstadisticasResponse;
import com.mza_agrotours.backend.dtos.estadisticasproductor.KpisDTO;
import com.mza_agrotours.backend.dtos.estadisticasproductor.SerieDTO;
import com.mza_agrotours.backend.enums.PeriodoRango;
import com.mza_agrotours.backend.services.EstadisticaProductorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/estadisticas-productores/{establecimientoId}")
public class EstadisticaProductorController {

    @Autowired
    EstadisticaProductorService estadisticaProductorService;

    @GetMapping
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<EstadisticasResponse>> obtenerEstadisticas(
            @PathVariable UUID establecimientoId,
            @RequestParam PeriodoRango periodo) {
        EstadisticasResponse response = estadisticaProductorService.calcularEstadisticas(establecimientoId, periodo);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
    /**
     * Obtiene todos los KPIs (ocupación, beneficios y cancelación).
     * GET /estadisticas-productores/{establecimientoId}/kpis?periodo=TREINTA_D
     */
    @GetMapping("/kpis")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<KpisDTO>> obtenerKpis(
            @PathVariable UUID establecimientoId,
            @RequestParam PeriodoRango periodo) {
        KpisDTO kpis = estadisticaProductorService.calcularKpis(establecimientoId, periodo);
        return ResponseEntity.ok(ApiResponse.ok(kpis));
    }

    /**
     * Obtiene únicamente los KPIs de ocupación.
     * GET /estadisticas-productores/{establecimientoId}/kpis/ocupacion?periodo=TREINTA_D
     */
    @GetMapping("/kpis/ocupacion")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<KpisDTO>> obtenerKpisOcupacion(
            @PathVariable UUID establecimientoId,
            @RequestParam PeriodoRango periodo) {
        KpisDTO kpis = estadisticaProductorService.calcularKpisOcupacion(establecimientoId, periodo);
        return ResponseEntity.ok(ApiResponse.ok(kpis));
    }

    /**
     * Obtiene únicamente los KPIs de beneficios.
     * GET /estadisticas-productores/{establecimientoId}/kpis/beneficios?periodo=TREINTA_D
     */
    @GetMapping("/kpis/beneficios")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<KpisDTO>> obtenerKpisBeneficios(
            @PathVariable UUID establecimientoId,
            @RequestParam PeriodoRango periodo) {
        KpisDTO kpis = estadisticaProductorService.calcularKpisBeneficios(establecimientoId, periodo);
        return ResponseEntity.ok(ApiResponse.ok(kpis));
    }

    /**
     * Obtiene únicamente los KPIs de cancelación.
     * GET /estadisticas-productores/{establecimientoId}/kpis/cancelacion?periodo=TREINTA_D
     */
    @GetMapping("/kpis/cancelacion")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<KpisDTO>> obtenerKpisCancelacion(
            @PathVariable UUID establecimientoId,
            @RequestParam PeriodoRango periodo) {
        KpisDTO kpis = estadisticaProductorService.calcularKpisCancelacion(establecimientoId, periodo);
        return ResponseEntity.ok(ApiResponse.ok(kpis));
    }

    /**
     * Obtiene únicamente la serie temporal para el gráfico de barras.
     * GET /estadisticas-productores/{establecimientoId}/serie?periodo=TREINTA_D
     */
    @GetMapping("/serie")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<SerieDTO>> obtenerSerie(
            @PathVariable UUID establecimientoId,
            @RequestParam PeriodoRango periodo) {
        SerieDTO serie = estadisticaProductorService.calcularSerie(establecimientoId, periodo);
        return ResponseEntity.ok(ApiResponse.ok(serie));
    }

    /**
     * Obtiene únicamente el desglose de performance por actividad.
     * GET /estadisticas-productores/{establecimientoId}/actividades?periodo=TREINTA_D
     */
    @GetMapping("/actividades")
    @PreAuthorize("@estAuth.esTitular(authentication, #establecimientoId)")
    public ResponseEntity<ApiResponse<List<ActividadPerformanceDTO>>> obtenerPerformanceActividades(
            @PathVariable UUID establecimientoId,
            @RequestParam PeriodoRango periodo) {
        List<ActividadPerformanceDTO> actividades = estadisticaProductorService.calcularPerformanceActividades(establecimientoId, periodo);
        return ResponseEntity.ok(ApiResponse.ok(actividades));
    }
}
