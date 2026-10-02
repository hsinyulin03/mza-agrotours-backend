package com.mza_agrotours.backend.repositories.faq;

import com.mza_agrotours.backend.dtos.faq.DTOCategoriaFaqFiltro;
import com.mza_agrotours.backend.entities.faq.CategoriaFAQ;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaFaqRepository extends BaseEntityRepository<CategoriaFAQ, Long> {
    Optional<CategoriaFAQ> findByNombre(CategoriaFAQNombre nombre);

    @Query("""
        SELECT new com.mza_agrotours.backend.dtos.faq.DTOCategoriaFaqFiltro(
            c.nombre,
            COUNT(f)
        )
        FROM CategoriaFAQ c
        LEFT JOIN FAQ f ON f.categoriaFAQ = c AND f.fechaHoraBaja IS NULL
        GROUP BY c.nombre
        ORDER BY c.nombre
        """)
    List<DTOCategoriaFaqFiltro> listarCategoriasConConteo();
}
