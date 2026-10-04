package com.mza_agrotours.backend.entities.faq;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.enums.CategoriaFAQNombre;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaFAQ extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaFAQNombre nombre;
}
