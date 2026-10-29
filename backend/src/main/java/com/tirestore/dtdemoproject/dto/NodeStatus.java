package com.tirestore.dtdemoproject.dto;

import java.time.Instant;
import java.util.List;

// odpoved pre stavovy panel uzlov (GET /api/status)
// outboxPending = -1, ak databaza nie je dostupna
public record NodeStatus(
        String code,
        String role,
        boolean dbUp,
        Instant startedAt,
        long uptimeSeconds,
        long outboxPending,
        List<PeerStatus> peers) {
}
