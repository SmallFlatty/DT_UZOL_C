package com.tirestore.dtdemoproject.service;

import com.tirestore.dtdemoproject.config.NodeProperties;
import com.tirestore.dtdemoproject.dto.NodeStatus;
import com.tirestore.dtdemoproject.dto.PeerStatus;
import com.tirestore.dtdemoproject.monitor.PeerMonitor;
import com.tirestore.dtdemoproject.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

// data pre stavovy panel uzlov
@Service
@RequiredArgsConstructor
public class StatusService {

    private final NodeProperties node;
    private final OutboxRepository outboxRepository;
    private final PeerMonitor peerMonitor;
    private final DataSource dataSource;

    private final Instant startedAt = Instant.now();

    public NodeStatus status() {
        boolean dbUp = isDbUp();
        long total = -1;
        List<PeerStatus> peers = new ArrayList<>();

        for (PeerMonitor.PeerState p : peerMonitor.peers()) {
            long pending = -1;
            if (dbUp) {
                try {
                    pending = outboxRepository.countByTarget(p.getUrl());
                } catch (Exception ignored) {
                    // DB spadla medzi kontrolou a dotazom
                }
            }
            peers.add(new PeerStatus(p.getUrl(), p.getCode(), p.getRole(), p.getState(),
                    p.getLastSeen(), pending, p.getLastSyncOk(), p.getLastError()));
        }

        if (dbUp) {
            try {
                total = outboxRepository.count();
            } catch (Exception ignored) {
                // total ostane -1
            }
        }

        long uptime = Duration.between(startedAt, Instant.now()).getSeconds();
        return new NodeStatus(String.valueOf(node.code()), node.role().name(), dbUp, startedAt, uptime, total, peers);
    }

    private boolean isDbUp() {
        try (Connection c = dataSource.getConnection()) {
            return c.isValid(1);
        } catch (Exception e) {
            return false;
        }
    }
}
