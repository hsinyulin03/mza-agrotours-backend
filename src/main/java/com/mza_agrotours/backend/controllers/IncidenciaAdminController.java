package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.incidencia.DTOIncidenciaFiltro;
import com.mza_agrotours.backend.dtos.incidencia.DTOIncidenciaGestionListado;
import com.mza_agrotours.backend.services.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/incidencias")
public class IncidenciaAdminController {
    @Autowired
    private IncidenciaService incidenciaService;
    @GetMapping
    public ResponseEntity<Page<DTOIncidenciaGestionListado>> listarIncidencias(
            @ModelAttribute DTOIncidenciaFiltro filtro,
            @PageableDefault(sort = "fechaHoraInicio", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DTOIncidenciaGestionListado> paginaResultado = incidenciaService.obtenerIncidencias(filtro, pageable);
        return ResponseEntity.ok(paginaResultado);
    }
}
