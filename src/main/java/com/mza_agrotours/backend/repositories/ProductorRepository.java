package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.dtos.productor.Productor;
import com.mza_agrotours.backend.entities.Usuario;

import java.util.List;
import java.util.UUID;

public interface ProductorRepository extends BaseEntityRepository<Productor, UUID> {
    List<Productor> findByUsuarioAndFechaHoraBajaIsNull(Usuario usuario);
}