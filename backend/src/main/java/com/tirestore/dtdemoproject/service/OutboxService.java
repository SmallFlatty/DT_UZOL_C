package com.tirestore.dtdemoproject.service;

import com.tirestore.dtdemoproject.config.NodeProperties;
import com.tirestore.dtdemoproject.entity.OutboxEntity;
import com.tirestore.dtdemoproject.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final NodeProperties node;
    private final ObjectMapper objectMapper;

    // pre kazdy peer jeden riadok v outboxe, vsetky s rovnakym eventId
    public void add(String entity, String operation, UUID entityId, Object data) {
        String payload = null;
        if (data != null) {
            payload = objectMapper.writeValueAsString(data);
        }

        UUID eventId = UUID.randomUUID();

        for (String peer : node.peers()) {
            OutboxEntity e = new OutboxEntity();
            e.setTarget(peer);
            e.setEntity(entity);
            e.setOperation(operation);
            e.setEntityId(entityId);
            e.setEventId(eventId);
            e.setPayload(payload);
            e.setCreatedAt(OffsetDateTime.now());
            outboxRepository.save(e);
        }
        log.info("OUTBOX queued {} {} {} for {} peer(s), event {}", entity, operation, entityId, node.peers().size(), eventId);
    }
}
