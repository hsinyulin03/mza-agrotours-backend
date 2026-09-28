package com.mza_agrotours.backend.config.outbox;

import com.mza_agrotours.backend.entities.Outbox;

public interface OutboxStrategy {
    void resolver(Outbox outbox) throws Exception;
}
