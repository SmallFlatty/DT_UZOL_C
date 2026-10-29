package com.tirestore.dtdemoproject.controller;

import com.tirestore.dtdemoproject.config.NodeProperties;
import com.tirestore.dtdemoproject.entity.NotificationEntity;
import com.tirestore.dtdemoproject.entity.ParcelEntity;
import com.tirestore.dtdemoproject.service.ParcelQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// citanie - pouzivaju vsetky tri fronty
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelQueryService parcelQueryService;
    private final NodeProperties node;

    // front si zisti, na ktorom uzle je a co ma zobrazit
    @GetMapping("/node")
    public Map<String, Object> nodeInfo() {
        return Map.of("code", node.code(), "role", node.role());
    }

    @GetMapping("/parcels")
    public List<ParcelEntity> getAll() {
        return parcelQueryService.findAll();
    }

    @GetMapping("/parcels/tracking/{trackingNum}")
    public ParcelEntity getByTracking(@PathVariable String trackingNum) {
        return parcelQueryService.findByTrackingNum(trackingNum);
    }

    @GetMapping("/parcels/{id}/notifications")
    public List<NotificationEntity> getNotifications(@PathVariable UUID id) {
        return parcelQueryService.findNotifications(id);
    }
}
