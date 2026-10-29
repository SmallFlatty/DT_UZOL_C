package com.tirestore.dtdemoproject.worker;

import com.tirestore.dtdemoproject.dto.SyncEvent;
import com.tirestore.dtdemoproject.entity.OutboxEntity;
import com.tirestore.dtdemoproject.monitor.PeerMonitor;
import com.tirestore.dtdemoproject.repository.OutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// kazdych 5 s posle outbox peerom; riadok zmaze az po uspesnom odoslani (trvale ulozenie neodoslanych operacii)
@Slf4j
@Component
public class SyncWorker {

    private final OutboxRepository outboxRepository;
    private final PeerMonitor peerMonitor;
    private final RestClient restClient;

    @Value("${config.sync-token}")
    private String syncToken;

    public SyncWorker(OutboxRepository outboxRepository, PeerMonitor peerMonitor) {
        this.outboxRepository = outboxRepository;
        this.peerMonitor = peerMonitor;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Scheduled(fixedDelay = 5000)
    public void send() {
        List<OutboxEntity> pending = outboxRepository.findAll(Sort.by("id"));
        if (pending.isEmpty()) {
            return;
        }

        // uzly, ktore v tomto kole nefunguju
        Set<String> failed = new HashSet<>();

        for (OutboxEntity e : pending) {
            String target = e.getTarget();
            if (failed.contains(target)) {
                continue; // aby sa nepomiesalo poradie
            }

            try {
                SyncEvent event = new SyncEvent(e.getEntity(), e.getOperation(), e.getEntityId(), e.getPayload(), e.getEventId());

                restClient.post()
                        .uri(target + "/api/sync")
                        .header("X-Sync-Token", syncToken)
                        .body(event)
                        .retrieve()
                        .toBodilessEntity();

                outboxRepository.delete(e);

                if (peerMonitor.syncSucceeded(target)) {
                    log.info("SYNC RECOVERED -> {} - odosielam frontu", target);
                }
                log.info("SYNC OK {} {} {} -> {}", e.getEntity(), e.getOperation(), e.getEntityId(), target);
            } catch (Exception ex) {
                failed.add(target);
                if (peerMonitor.syncFailed(target, ex.getMessage())) {
                    log.warn("SYNC FAIL -> {} ({}), operacie cakaju v outboxe (celkom {})", target, ex.getMessage(), pending.size());
                } else {
                    log.debug("SYNC FAIL -> {} ({})", target, ex.getMessage());
                }
            }
        }
    }
}
