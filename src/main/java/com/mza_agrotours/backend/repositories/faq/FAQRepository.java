package com.mza_agrotours.backend.repositories.faq;

import com.mza_agrotours.backend.entities.faq.FAQ;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FAQRepository extends BaseEntityRepository <FAQ, Long> {
    @Query("""
    select f from FAQ f
    left join f.categoriaFAQ cf
    where f.fechaHoraBaja is null
      and (:categoria is null or cf.nombre = :categoria)
      and (lower(f.pregunta)  like lower(concat('%', :busqueda, '%'))
           or lower(f.respuesta) like lower(concat('%', :busqueda, '%')))
""")
    Page<FAQ> listarActivas(@Param("categoria") CategoriaFAQNombre categoria,
                            @Param("busqueda") String busqueda,
                            Pageable pageable);
    Optional<FAQ> findByIdAndFechaHoraBajaIsNull(UUID id);
}
