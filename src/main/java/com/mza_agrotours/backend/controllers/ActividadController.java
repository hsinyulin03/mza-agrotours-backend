package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.actividad.*;
import com.mza_agrotours.backend.entities.actividad.Actividad;
import com.mza_agrotours.backend.services.ActividadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/actividad")
public class ActividadController extends BaseEntityControllerImpl<Actividad, ActividadService>{
    @Autowired
    private ActividadService servicio;

    //US-ACT-03
    @PostMapping("/alta")
    public ResponseEntity<?> crearActividadConDetalles(@Valid @RequestBody DTOActividadAlta dto) {

        try {
            servicio.altaActividad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("{\" Actividad añadida correctamente }\"");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }

    }

    //US-ACT-02: Consultar detalle de una actividad
    @GetMapping("/getDetalles/{id}")
    public ResponseEntity<?> obtenerDetalleActividad(@PathVariable Long id) {

        try{
            return ResponseEntity.status(HttpStatus.OK).body(servicio.obtenerDetallePorId(id));

        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }

    }

    //US-ACT-06: Listado de actividades de un establecimiento - Vista productor
    @GetMapping("/listado-actividades-productor")
    public ResponseEntity<?> obtenerListadoProductor() throws Exception {

        try {
            List<DTOActividades> listado = servicio.obtenerListadoActividades();
            return ResponseEntity.status(HttpStatus.OK).body(listado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }

    }

    //US-ACT-07: Consultar todos los días disponibles para una actividad
    @GetMapping("/actividaDia/{id}")
    public ResponseEntity<?> obtenerCalendarioInteractvo(
            @PathVariable Long id,
            @RequestParam int mes,
            @RequestParam int anio) throws Exception {
        try {
            DTOCalendarioActividadDia detalle = servicio.obtenerDetalleCalendario(id, mes, anio);
            return ResponseEntity.status(HttpStatus.OK).body(detalle);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }

    }

    //US-ACT-12: Listado de actividades de la plataforma - vista del visitante
    @GetMapping("/explorar")
    public ResponseEntity <?> explorarActividades() throws Exception {
        try {
            List<DTOListadoActividadVisitante> listado = servicio.explorarActividades();
            return ResponseEntity.status(HttpStatus.OK).body(listado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}
