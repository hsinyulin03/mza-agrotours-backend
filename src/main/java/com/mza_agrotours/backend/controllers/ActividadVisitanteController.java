package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.UsuarioAuthDetails;
import com.mza_agrotours.backend.dtos.actividad.DTOActividadDetalleResponse;
import com.mza_agrotours.backend.dtos.actividad.DTOFiltro;
import com.mza_agrotours.backend.dtos.actividad.DTOListadoActividadVisitanteResponse;
import com.mza_agrotours.backend.dtos.actividad.InfoParaReservarDTO;
import com.mza_agrotours.backend.services.ActividadService;
import org.springframework.beans.factory.annotation.Autowired;
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

    //US-ACT-02: Consultar detalle de una actividad
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalleActividad(@PathVariable UUID id) throws Exception {

        DTOActividadDetalleResponse detalle = servicio.obtenerDetallePorId(id);
        return ResponseEntity.ok(ApiResponse.ok(detalle));
    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
    @GetMapping("/explorar")
    public ResponseEntity<?> explorarActividades(@RequestParam(required = false) List<UUID> cultivosIds,
                                                 @RequestParam(required = false) UUID departamentoId) throws Exception {
        List<DTOListadoActividadVisitanteResponse> listado = servicio.explorarActividades(cultivosIds, departamentoId);
        return ResponseEntity.ok(ApiResponse.ok(listado));
    }
    //Obtener el filtro de departamentos
    @GetMapping("/departamentos")
    public ResponseEntity<?> obtenerFiltroDepartamentos() {
        List<DTOFiltro> filtrosDpto = servicio.obtenerFiltroDepartamentos();
        return ResponseEntity.ok(ApiResponse.ok(filtrosDpto));
    }
    //Obtener el filtro de cultivos
    @GetMapping("/cultivos")
    public ResponseEntity<?> obtenerFiltroCultivos() {
        List<DTOFiltro> filtrosCultivo = servicio.obtenerFiltroCultivos();
        return ResponseEntity.ok(ApiResponse.ok(filtrosCultivo));
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
