package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.enums.EstadoReservaNombre;
import com.mza_agrotours.backend.services.ActividadService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<ApiResponse<Page<DTOActividadesResponse>>> obtenerListadoProductor(@PathVariable UUID establecimientoId,
                                                     @RequestParam(required = false) String busqueda,
                                                     @RequestParam(required = false) EstadoActividadNombre estado,
                                                     @PageableDefault(page = 0, size = 10, sort = {"nombre", "id"}, direction = Sort.Direction.ASC) Pageable pageable) throws Exception {


        Page<DTOActividadesResponse> listado = servicio.obtenerListadoActividades(establecimientoId, busqueda, estado, pageable);
        return ResponseEntity.ok(ApiResponse.ok(listado));

    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    @GetMapping("/{actividadId}/dias")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOCalendarioActividadDiaResponse>> obtenerCalendarioInteractvo(
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

    @PatchMapping("/{actividadId}/estado")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOCambioEstadoActividadResponse>> cambiarEstadoPublicacion(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @Valid @RequestBody DTOCambioEstadoActividad dto) {
        DTOCambioEstadoActividadResponse response = servicio.cambiarEstadoActividad(establecimientoId, actividadId, dto);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    //US-ACT-08: Resumen del día (encabezado de la pantalla)
    @GetMapping("/{actividadId}/dias/{actividadDiaId}/resumen")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOListadoReservasResumenResponse>> obtenerResumenDelDia(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @PathVariable UUID actividadDiaId) {

        DTOListadoReservasResumenResponse resumen = servicio.obtenerResumenDelDia(actividadId, actividadDiaId);
        return ResponseEntity.ok(ApiResponse.ok(resumen));
    }
    //US-ACT-08: Listado paginado de reservas del día
    @GetMapping("/{actividadId}/dias/{actividadDiaId}/reservas")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<Page<DTODetalleReservaCard>>> obtenerReservasDelDia(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @PathVariable UUID actividadDiaId,
            @RequestParam(required = false) EstadoReservaNombre estado,
            @PageableDefault(page = 0, size = 10, sort = {"fechaHoraInicio", "id"}, direction = Sort.Direction.ASC) Pageable pageable) {

        Page<DTODetalleReservaCard> reservas = servicio.obtenerReservasDelDia(actividadId, actividadDiaId, estado, pageable);
        return ResponseEntity.ok(ApiResponse.ok(reservas));
    }

    //US-ACT-08: Filtro de estados de reserva
    @GetMapping("/{actividadId}/dias/{actividadDiaId}/reservas/estados")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<List<DTOFiltro>>> obtenerFiltroEstadosReserva(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @PathVariable UUID actividadDiaId) {

        List<DTOFiltro> filtros = servicio.obtenerFiltroEstadosReserva(actividadId, actividadDiaId);
        return ResponseEntity.ok(ApiResponse.ok(filtros));
    }

    //US-ACT-11: Calendario para gestionar los días de una actividad
    @GetMapping("/{actividadId}/dias/calendario")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOCalendarioGestionDiasResponse>> obtenerCalendarioGestionDias(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @RequestParam @Min(value = 1, message = "El mes debe ser mayor o igual a 1")
            @Max(value = 12, message = "El mes debe ser menor o igual a 12") int mes,
            @RequestParam int anio) {

        DTOCalendarioGestionDiasResponse calendario = servicio.obtenerCalendarioGestionDias(actividadId, mes, anio);
        return ResponseEntity.ok(ApiResponse.ok(calendario));
    }

    //US-ACT-11: Agregar un día puntual
    @PostMapping("/{actividadId}/dias")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOActividadDiaResponse>> agregarDia(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @Valid @RequestBody DTOActividadDiaAlta dto) {

        DTOActividadDiaResponse dia = servicio.agregarUnActividadDia(establecimientoId, actividadId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dia));
    }

    //US-ACT-11: Previsualizar un lote de días para mostrar al productor en tiempo real los días que se crearán y los días que se descartan
    @PostMapping("/{actividadId}/dias/lote/previsualizacion")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOPrevisualizacionLoteResponse>> previsualizarLote(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @Valid @RequestBody DTOActividadDiasLote dto) {

        DTOPrevisualizacionLoteResponse previsualizacion = servicio.previsualizarLote(actividadId, dto);
        return ResponseEntity.ok(ApiResponse.ok(previsualizacion));
    }
    //US-ACT-11: Agregar días por lote
    @PostMapping("/{actividadId}/dias/lote")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOPrevisualizacionLoteResponse>> agregarLote(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @Valid @RequestBody DTOActividadDiasLote dto) {

        DTOPrevisualizacionLoteResponse resultado = servicio.agregarLoteActividadDias(establecimientoId, actividadId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(resultado));
    }

    //US-ACT-11: Modificar el cupo de un día
    @PatchMapping("/{actividadId}/dias/{actividadDiaId}/cupo")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOActividadDiaResponse>> modificarCupoDia(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId,
            @PathVariable UUID actividadDiaId,
            @Valid @RequestBody DTOActividadDiaUpdateCupo dto) {

        DTOActividadDiaResponse dia = servicio.modificarCupoDia(establecimientoId, actividadId, actividadDiaId, dto);
        return ResponseEntity.ok(ApiResponse.ok(dia));
    }


    //US-ACT-11: Datos de referencia para el formulario de agregar días
    @GetMapping("/{actividadId}/dias/configuracion")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOConfiguracionDiasResponse>> obtenerConfiguracionDias(
            @PathVariable UUID establecimientoId,
            @PathVariable UUID actividadId) {

        DTOConfiguracionDiasResponse configuracion = servicio.obtenerConfiguracionDias(actividadId);
        return ResponseEntity.ok(ApiResponse.ok(configuracion));
    }


    @DeleteMapping("/{actividadId}")
    @PreAuthorize("@estAuth.tienePermisoSobreActividad(authentication, #establecimientoId, #actividadId, T(com.mza_agrotours.backend.enums.PermisoCodigo).GESTIONAR_ACTIVIDAD)")
    public ResponseEntity<ApiResponse<DTOBajaActividadResponse>> darBajaActividad(@PathVariable UUID establecimientoId, @PathVariable UUID actividadId) {
        DTOBajaActividadResponse response = servicio.darBajaActividad(establecimientoId,actividadId );
        return ResponseEntity.ok(ApiResponse.ok(response));

    }

}
