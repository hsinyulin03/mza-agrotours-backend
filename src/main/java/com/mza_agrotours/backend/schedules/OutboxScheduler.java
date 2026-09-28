package com.mza_agrotours.backend.schedules;

import com.mza_agrotours.backend.services.outbox.OutboxService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {
    private final OutboxService outboxService;

    public OutboxScheduler(OutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 1000L)
    public void resolverPendientes() {
        this.outboxService.resolverPendientes();
    }
}
