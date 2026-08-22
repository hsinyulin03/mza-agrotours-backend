package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.actividad.DTOActividadDetalleResponse;
import com.mza_agrotours.backend.dtos.actividad.DTOListadoActividadVisitanteResponse;
import com.mza_agrotours.backend.dtos.reservas.InfoParaReservarDTO;
import com.mza_agrotours.backend.services.ActividadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    //US-RESE-01: Reservar actividad - Información para reservar
    @GetMapping("/{id}/reservar")
    public ResponseEntity<ApiResponse<InfoParaReservarDTO>> infoParaReservar(@PathVariable UUID id){
        InfoParaReservarDTO infoParaReservar = servicio.getInfoParaReservar(id);
        return ResponseEntity.ok(ApiResponse.ok(infoParaReservar));
    }

}
