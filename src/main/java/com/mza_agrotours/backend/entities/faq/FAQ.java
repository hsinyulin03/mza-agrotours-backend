package com.mza_agrotours.backend.entities.faq;

import com.mza_agrotours.backend.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FAQ extends BaseEntity {
    @Column(nullable = false, length = 160)
    private String pregunta;

    @Column(nullable = false, length = 700)
    private String respuesta;

    private LocalDateTime fechaHoraBaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_faq_id", nullable = false)
    private CategoriaFAQ categoriaFAQ;

}
