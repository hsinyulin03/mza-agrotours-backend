package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.services.ActividadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/establecimientos/{establecimientoId}/actividades")
@Validated
public class ActividadProductorController {

    @Autowired
    private ActividadService servicio;

    // US-ACT-03: Dar de alta una actividad
    @PostMapping("/alta")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<?> crearActividadConDetalles(@PathVariable UUID establecimientoId,
                                                       @Valid @RequestBody DTOActividadAlta dto) throws Exception {
        DTOActividadAltaResponse nuevaActividad = servicio.altaActividad(establecimientoId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(nuevaActividad));
    }


    //US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    @GetMapping
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    //busqueda es lo que ingresa en el search bar y estado es para filtrar actividad por estado
    public ResponseEntity<?> obtenerListadoProductor(@PathVariable UUID establecimientoId,
                                                     @RequestParam(required = false) String busqueda,
                                                     @RequestParam(required = false) EstadoActividadNombre estado) throws Exception {


        List<DTOActividadesResponse> listado = servicio.obtenerListadoActividades(establecimientoId, busqueda, estado);
        return ResponseEntity.ok(ApiResponse.ok(listado));

    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    @GetMapping("/{actividadId}/dias")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<?> obtenerCalendarioInteractvo(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @RequestParam @Min(value = 1, message = "El mes debe ser mayor o igual a 1")
            @Max(value = 12, message = "El mes debe ser menor o igual a 12") int mes,
            @RequestParam int anio) throws Exception {

        DTOCalendarioActividadDiaResponse detalle = servicio.obtenerDetalleCalendario(actividadId, mes, anio);
        return ResponseEntity.ok(ApiResponse.ok(detalle));

    }


    //US-ACT-04: Modificar Actividad
    @GetMapping("/edit/{actividadId}")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<?> obtenerActividadPorId(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId) {
        DTOActividadGetResponse response = servicio.obtenerActividadPorId(actividadId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    //US-ACT-04: Modificar Actividad
    @PutMapping("/edit/{actividadId}")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<?> modificarActividad(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @Valid @RequestBody DTOActividadUpdate dto) {
        DTOActividadGetResponse res = servicio.modificarActividad(establecimientoId, actividadId, dto);
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/estados")
    @PreAuthorize("@estAuth.tienePermiso(authentication, #establecimientoId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<?> obtenerFiltroEstadoActividad(@PathVariable UUID establecimientoId) {
        List<DTOFiltro> estadosRes = servicio.obtenerFiltroEstadoActividad(establecimientoId);
        return ResponseEntity.ok(ApiResponse.ok(estadosRes));
    }

}
