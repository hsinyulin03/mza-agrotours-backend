package com.mza_agrotours.backend.controllers;

import com.mza_agrotours.backend.dtos.ApiResponse;
import com.mza_agrotours.backend.dtos.faq.*;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import com.mza_agrotours.backend.services.FAQService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/faq")
public class FAQAdminController {
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

    @GetMapping("/{faqId}")
    public ResponseEntity<ApiResponse<FaqFormAMRequest>> obtenerParaEditar(@PathVariable UUID faqId) {
        return ResponseEntity.ok(ApiResponse.ok(faqService.getformAMfaq(faqId)));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<FaqResponse>> crear(@Valid @RequestBody FaqAMRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(faqService.createFaq(request)));
    }

    @PutMapping("/{faqId}")
    public ResponseEntity<ApiResponse<FaqResponse>> actualizar(
            @PathVariable UUID faqId,
            @Valid @RequestBody FaqAMRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(faqService.updateFaq(request, faqId)));
    }

    @DeleteMapping("/{faqId}")
    public ResponseEntity<ApiResponse<FaqDeleteResponse>> eliminar(@PathVariable UUID faqIdid) {
        return ResponseEntity.ok(ApiResponse.ok(faqService.deleteFaq(faqIdid)));
    }

}
