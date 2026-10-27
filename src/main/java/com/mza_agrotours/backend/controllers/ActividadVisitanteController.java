package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.dtos.calificacion.ReseniaCardDTO;
import com.mza_agrotours.backend.services.ActividadService;
import com.mza_agrotours.backend.services.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/actividades")
public class ActividadVisitanteController {

    @Autowired
    private ActividadService servicio;

    @Autowired
    private CalificacionService calificacionService;

    //US-ACT-02: Consultar detalle de una actividad
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DTOActividadDetalleResponse>> obtenerDetalleActividad(@PathVariable UUID id) throws Exception {

        DTOActividadDetalleResponse detalle = servicio.obtenerDetallePorId(id);
        return ResponseEntity.ok(ApiResponse.ok(detalle));
    }

    //US-ACT-01: Listado de todas las reseñas de la actividad - listado paginado (botón "ver todas")
    //NOTA: el panel de distribucion de barras y promedio general de calificacion se puede obtener de resumenResenias del GET /actividades/{id}
    @GetMapping("/{id}/resenias")
    public ResponseEntity<ApiResponse<Page<ReseniaCardDTO>>> obtenerResenias(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer puntaje,
            @RequestParam(required = false) String orden,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<ReseniaCardDTO> resenias = calificacionService.obtenerResenias(id, puntaje, orden, pageable);
        return ResponseEntity.ok(ApiResponse.ok(resenias));
    }
    //US-ACT-01: Filtro de puntajes del modal de reseñas
    @GetMapping("/{idActividad}/resenias/puntajes")
    public ResponseEntity<ApiResponse<List<DTOFiltro>>> obtenerFiltroPuntajes(@PathVariable UUID idActividad) {
        List<DTOFiltro> filtros = calificacionService.obtenerFiltroPuntajes(idActividad);
        return ResponseEntity.ok(ApiResponse.ok(filtros));
    }

    //US-ACT-01: Filtro de orden del modal de reseñas
    @GetMapping("/{idActividad}/resenias/orden")
    public ResponseEntity<ApiResponse<List<DTOFiltro>>> obtenerFiltroOrden(@PathVariable UUID idActividad) {
        List<DTOFiltro> filtros = calificacionService.obtenerFiltroOrden(idActividad);
        return ResponseEntity.ok(ApiResponse.ok(filtros));
    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
    @GetMapping("/explorar")
    public ResponseEntity<ApiResponse<Page<DTOListadoActividadVisitanteResponse>>> explorarActividades(
                                                 FiltroActividadRequest filtro,
                                                 @PageableDefault(page = 0, size = 10, sort = {"nombre", "id"}, direction = Sort.Direction.ASC) Pageable pageable) throws Exception {
        Page<DTOListadoActividadVisitanteResponse> listado = servicio.explorarActividades(filtro, pageable);
        return ResponseEntity.ok(ApiResponse.ok(listado));
    }
    //Landing: actividades destacadas
    @GetMapping("/destacadas")
    public ResponseEntity<ApiResponse<List<DTOListadoActividadVisitanteResponse>>> obtenerActividadesDestacadas() {
        return ResponseEntity.ok(ApiResponse.ok(servicio.obtenerActividadesDestacadas()));
    }

    @GetMapping("/filtros")
    public ResponseEntity<ApiResponse<FiltroActividadResponse>> obtenerFiltros(FiltroActividadRequest filtro) {
        return ResponseEntity.ok(ApiResponse.ok(servicio.obtenerFiltrosActividad(filtro)));
    }

    //US-RESE-01: Reservar actividad - Información para reservar
    @GetMapping("/{id}/reservar")
    public ResponseEntity<ApiResponse<InfoParaReservarDTO>> infoParaReservar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UsuarioAuthDetails usuarioAuthDetails
    ){
        String email = usuarioAuthDetails.getEmail();
        InfoParaReservarDTO infoParaReservar = servicio.getInfoParaReservar(id, email);
        return ResponseEntity.ok(ApiResponse.ok(infoParaReservar));
    }

}
