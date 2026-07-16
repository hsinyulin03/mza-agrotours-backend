package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.enums.EstadoActividadNombre;
import com.mza_agrotours.backend.services.ActividadService;
import com.mza_agrotours.backend.services.RangoEtarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/actividades")
@Validated
public class ActividadController {

    @Autowired
    private ActividadService servicio;

    // US-ACT-03: Dar de alta una actividad
    @PostMapping("/alta")
    public ResponseEntity<?> crearActividadConDetalles(@Valid @RequestBody DTOActividadAlta dto) throws Exception{
        //Está bien devolver un dto?
        DTOActividadDetalle nuevaActividad = servicio.altaActividad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(nuevaActividad));
    }

    //US-ACT-02: Consultar detalle de una actividad
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetalleActividad(@PathVariable UUID id) throws Exception {

        DTOActividadDetalle detalle = servicio.obtenerDetallePorId(id);
        return ResponseEntity.ok(ApiResponse.ok(detalle));
    }

    //US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    @GetMapping
    //busqueda es lo que ingresa en el search bar y estado es para filtrar actividad por estado
    public ResponseEntity<?> obtenerListadoProductor(@RequestParam(required = false) String busqueda,
                                                     @RequestParam(required = false) EstadoActividadNombre estado) throws Exception {


        List<DTOActividades> listado = servicio.obtenerListadoActividades(busqueda, estado);
        return ResponseEntity.ok(ApiResponse.ok(listado));

    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    @GetMapping("/{id}/dias")
    public ResponseEntity<?> obtenerCalendarioInteractvo(
            @PathVariable UUID id,
            @RequestParam @Min(value = 1, message = "El mes debe ser mayor o igual a 1")
                          @Max(value = 12, message = "El mes debe ser menor o igual a 12") int mes,
            @RequestParam int anio) throws Exception {

        DTOCalendarioActividadDia detalle = servicio.obtenerDetalleCalendario(id, mes, anio);
        return ResponseEntity.ok(ApiResponse.ok(detalle));

    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
    @GetMapping("/explorar")
    public ResponseEntity <?> explorarActividades() throws Exception {
        List<DTOListadoActividadVisitante> listado = servicio.explorarActividades();
        return ResponseEntity.ok(ApiResponse.ok(listado));
    }
}
