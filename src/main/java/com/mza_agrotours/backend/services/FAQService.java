package com.mza_agrotours.backend.services;

import com.mza_agrotours.backend.dtos.faq.*;

import java.util.List;
import com.mza_agrotours.backend.entities.faq.CategoriaFAQ;
import com.mza_agrotours.backend.entities.faq.FAQ;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import com.mza_agrotours.backend.exceptions.EntityNotFoundException;
import com.mza_agrotours.backend.mappers.FAQmapper;
import com.mza_agrotours.backend.repositories.faq.CategoriaFaqRepository;
import com.mza_agrotours.backend.repositories.faq.FAQRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threeten.bp.LocalDate;

@Service
public class FAQService {
    @Autowired
    private FAQRepository faqRepository;
    @Autowired
    private CategoriaFaqRepository categoriaFaqRepository;
    @Autowired
    private FAQmapper faqMapper;

    @Transactional
    public FaqResponse createFaq(FaqAMRequest dto) {
        CategoriaFAQ categoria = validarCategoria(dto.getCategoria());

        FAQ faq = new FAQ();
        faq.setPregunta(dto.getPregunta());
        faq.setRespuesta(dto.getRespuesta());
        faq.setCategoriaFAQ(categoria);

        FAQ guardada = faqRepository.save(faq);
        FaqResponse response = new FaqResponse();
        response.setId(guardada.getId());
        response.setMessage("Pregunta agregada a la base de conocimiento ");
        return response;
    }
    @Transactional(readOnly = true)
    public FaqFormAMRequest getformAMfaq(Long id) {
        FAQ faq = buscarFaqPorId(id);
        FaqFormAMRequest dto = new FaqFormAMRequest();
        dto.setId(faq.getId());
        dto.setPregunta(faq.getPregunta());
        dto.setRespuesta(faq.getRespuesta());
        dto.setCategoria(faq.getCategoriaFAQ().getNombre());
        return dto;
    }

    @Transactional
    public FaqResponse updateFaq(FaqAMRequest dto, Long id) {
        FAQ faq = buscarFaqPorId(id);
        CategoriaFAQ categoria = validarCategoria(dto.getCategoria());
        faq.setPregunta(dto.getPregunta());
        faq.setRespuesta(dto.getRespuesta());
        faq.setCategoriaFAQ(categoria);
        FAQ guardada = faqRepository.save(faq);
        FaqResponse response = new FaqResponse();
        response.setId(guardada.getId());
        response.setMessage("Pregunta actualizada");
        return response;
    }
    @Transactional
    public FaqDeleteResponse deleteFaq(Long id) {
        FAQ faq = buscarFaqPorId(id);
        faq.setFechaHoraBaja(LocalDate.now());
        faqRepository.save(faq);
        FaqDeleteResponse response = new FaqDeleteResponse();
        response.setId(faq.getId());
        response.setMessage("Pregunta eliminada de la base de conocimiento");
        return response;
    }

    @Transactional(readOnly = true)
    public List<DTOCategoriaFaqFiltro> listCategorias() {
        return categoriaFaqRepository.listarCategoriasConConteo();
    }

    @Transactional(readOnly = true)
    public Page<DTOListadoAdminFaq> listFaq(CategoriaFAQNombre categoria, String busqueda, Pageable pageable) {
        String busquedaNormalizada = (busqueda != null && busqueda.isBlank()) ? null : busqueda;
        return faqRepository.listarActivas(categoria, busquedaNormalizada, pageable)
                .map(faqMapper::FAQtoDTOListadoAdminFaq);
    }
    




    public CategoriaFAQ validarCategoria(CategoriaFAQNombre categoriaNombre) {
        CategoriaFAQ categoria = categoriaFaqRepository.findByNombre(categoriaNombre)
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con nombre: " + categoriaNombre));
        return categoria;
    }

    public FAQ buscarFaqPorId(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ no encontrada con ID: " + id));
    }


}
