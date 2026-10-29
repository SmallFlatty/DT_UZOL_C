package com.tirestore.dtdemoproject.controller;

import com.tirestore.dtdemoproject.dto.SyncEvent;
import com.tirestore.dtdemoproject.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// sem posielaju zmeny ostatne uzly, nie front
@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @Value("${config.sync-token}")
    private String syncToken;

    @PostMapping
    public void sync(@RequestHeader("X-Sync-Token") String token, @RequestBody SyncEvent event) {
        if (!token.equals(syncToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Zly token");
        }
        syncService.apply(event);
    }
}
