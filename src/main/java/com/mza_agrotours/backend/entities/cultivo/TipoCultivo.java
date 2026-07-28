package com.mza_agrotours.backend.entities.cultivo;

import com.mza_agrotours.backend.entities.BaseEntity;
import com.mza_agrotours.backend.entities.receta.Receta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TipoCultivo extends BaseEntity {
    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length =500)
    private String descripcion;
    private LocalDateTime fechaHoraBaja;
    // guarda una lista de valores simples String en una tabla aparte
    // vinculada a TipoCultivo mediante la columna tipo_cultivo_id
    @ElementCollection
    @CollectionTable(name = "tipo_cultivo_beneficios", joinColumns = @JoinColumn(name = "tipo_cultivo_id"))
    @Column(name = "beneficio", length = 100)
    private List<String> beneficios = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tipo_cultivo_id")
    private List<InformacionNutricional> informacionNutricional = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tipo_cultivo_id")
    private List<EstacionalidadMes> estacionalidadMeses = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "tipo_cultivo_receta",
            joinColumns = @JoinColumn(name = "tipo_cultivo_id"),
            inverseJoinColumns = @JoinColumn(name = "receta_id")
    )
    private List<Receta> recetas = new ArrayList<>();


}
