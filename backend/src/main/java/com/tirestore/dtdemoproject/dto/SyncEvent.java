package com.tirestore.dtdemoproject.dto;

import java.util.UUID;

// eventId = idempotency kluc: ten isty event sa moze prijat viackrat, aplikuje sa len raz
public record SyncEvent(String entity, String operation, UUID entityId, String payload, UUID eventId) {
}
