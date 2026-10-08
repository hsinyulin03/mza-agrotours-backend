package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.faq.DTOCategoriaFaqFiltro;
import com.mza_agrotours.backend.dtos.faq.DTOListadoAdminFaq;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import com.mza_agrotours.backend.services.FAQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/faq")
public class FAQController {
    @Autowired
    private FAQService faqService;

    @GetMapping("/categorias")
    public ResponseEntity<ApiResponse<List<DTOCategoriaFaqFiltro>>> listarCategorias() {
        return ResponseEntity.ok(ApiResponse.ok(faqService.listCategorias()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DTOListadoAdminFaq>>> listar(
            @RequestParam(required = false) CategoriaFAQNombre categoria,
            @RequestParam(required = false) String busqueda,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(faqService.listFaq(categoria, busqueda, pageable)));
    }




}
