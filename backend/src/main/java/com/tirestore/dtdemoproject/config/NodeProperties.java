package com.tirestore.dtdemoproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// hodnoty z application.properties: config.code, config.role, config.peers
@ConfigurationProperties(prefix = "config")
public record NodeProperties(Character code, NodeRole role, List<String> peers) {

    public void requireRole(NodeRole required) {
        if (role != required) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nepovolené pre tento uzol");
        }
    }
}
