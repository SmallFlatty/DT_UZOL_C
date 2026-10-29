package com.tirestore.dtdemoproject.monitor;

import com.tirestore.dtdemoproject.config.NodeProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

// drzi stav susednych uzlov (dostupny / nedostupny, posledna synchronizacia) pre stavovy panel
// kazdé 3 s zavola GET /api/node na kazdom peerovi
@Slf4j
@Component
public class PeerMonitor {

    private final Map<String, PeerState> states = new LinkedHashMap<>(); // plni sa len v konstruktore
    private final RestClient restClient;

    public PeerMonitor(NodeProperties node) {
        for (String peer : node.peers()) {
            states.put(peer, new PeerState(peer));
        }

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(1));
        factory.setReadTimeout(Duration.ofSeconds(2));
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 3000)
    public void probe() {
        for (PeerState s : states.values()) {
            try {
                Map<?, ?> info = restClient.get().uri(s.getUrl() + "/api/node").retrieve().body(Map.class);
                String code = info == null ? null : String.valueOf(info.get("code"));
                String role = info == null ? null : String.valueOf(info.get("role"));
                if (s.markUp(code, role)) {
                    log.info("PEER UP   {} ({} / {})", s.getUrl(), code, role);
                }
            } catch (Exception ex) {
                if (s.markDown(ex.getMessage())) {
                    log.warn("PEER DOWN {} - {}", s.getUrl(), ex.getMessage());
                }
            }
        }
    }

    public Collection<PeerState> peers() {
        return states.values();
    }

    // true, ak sa synchronizacia s peerom prave obnovila po vypadku
    public boolean syncSucceeded(String url) {
        PeerState s = states.get(url);
        return s != null && s.syncSucceeded();
    }

    // true, ak synchronizacia s peerom prave zacala zlyhavat (aby sme nelogovali kazdych 5 s to iste)
    public boolean syncFailed(String url, String error) {
        PeerState s = states.get(url);
        return s != null && s.syncFailed(error);
    }

    @Getter
    public static class PeerState {
        private final String url;
        private volatile String state = "UNKNOWN"; // UP / DOWN / UNKNOWN
        private volatile String code;
        private volatile String role;
        private volatile Instant lastSeen;
        private volatile Instant lastSyncOk;
        private volatile String lastError;
        private volatile boolean syncFailing;

        PeerState(String url) {
            this.url = url;
        }

        // vracia true, ak sa stav zmenil
        synchronized boolean markUp(String code, String role) {
            boolean changed = !"UP".equals(state);
            this.state = "UP";
            this.code = code;
            this.role = role;
            this.lastSeen = Instant.now();
            this.lastError = null;
            return changed;
        }

        synchronized boolean markDown(String error) {
            boolean changed = !"DOWN".equals(state);
            this.state = "DOWN";
            this.lastError = error;
            return changed;
        }

        synchronized boolean syncSucceeded() {
            boolean recovered = syncFailing;
            this.syncFailing = false;
            this.state = "UP";
            this.lastSeen = Instant.now();
            this.lastSyncOk = Instant.now();
            return recovered;
        }

        synchronized boolean syncFailed(String error) {
            boolean newly = !syncFailing;
            this.syncFailing = true;
            this.lastError = error;
            return newly;
        }
    }
}
