package com.tirestore.dtdemoproject.controller;

import com.tirestore.dtdemoproject.dto.NodeStatus;
import com.tirestore.dtdemoproject.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// stavovy panel uzlov: tento uzol + susedia (dostupnost, fronta v outboxe, posledna synchronizacia)
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatusController {

    private final StatusService statusService;

    @GetMapping("/status")
    public NodeStatus status() {
        return statusService.status();
    }
}
