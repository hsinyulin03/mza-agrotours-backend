package com.mza_agrotours.backend.repositories.pago;

import com.mza_agrotours.backend.entities.pago.Reembolso;
import com.mza_agrotours.backend.repositories.BaseEntityRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReembolsoRepository extends BaseEntityRepository<Reembolso, UUID>{
}
