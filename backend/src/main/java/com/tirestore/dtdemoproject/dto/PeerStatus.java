package com.tirestore.dtdemoproject.dto;

import java.time.Instant;

// stav jedneho susedneho uzla z pohladu tohto uzla
// state: UP / DOWN / UNKNOWN, pending = kolko operacii caka v outboxe na tento peer
public record PeerStatus(
        String url,
        String code,
        String role,
        String state,
        Instant lastSeen,
        long pending,
        Instant lastSyncOk,
        String lastError) {
}
