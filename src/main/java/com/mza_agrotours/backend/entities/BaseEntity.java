package com.mza_agrotours.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@MappedSuperclass
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;
}
