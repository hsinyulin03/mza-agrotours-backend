package com.mza_agrotours.backend.repositories;

import com.mza_agrotours.backend.entities.Outbox;
import com.mza_agrotours.backend.enums.outbox.EstadoOutbox;

import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends BaseEntityRepository<Outbox, UUID> {
    List<Outbox> findByEstado(EstadoOutbox estado);
}
