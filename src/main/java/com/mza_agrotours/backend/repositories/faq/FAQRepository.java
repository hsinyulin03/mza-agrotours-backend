package com.mza_agrotours.backend.repositories.faq;

import com.mza_agrotours.backend.entities.faq.FAQ;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FAQRepository extends BaseEntityRepository <FAQ, Long> {
    @Query("""
        SELECT f FROM FAQ f
        WHERE f.fechaHoraBaja IS NULL
          AND (:categoria IS NULL OR f.categoriaFAQ.nombre = :categoria)
          AND (:busqueda IS NULL OR LOWER(f.pregunta) LIKE LOWER(CONCAT('%', :busqueda, '%'))
               OR LOWER(f.respuesta) LIKE LOWER(CONCAT('%', :busqueda, '%')))
        """)
    Page<FAQ> listarActivas(@Param("categoria") CategoriaFAQNombre categoria,
                            @Param("busqueda") String busqueda,
                            Pageable pageable);
}
