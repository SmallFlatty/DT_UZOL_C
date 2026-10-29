package com.tirestore.dtdemoproject.controller;

import com.tirestore.dtdemoproject.entity.NotificationEntity;
import com.tirestore.dtdemoproject.service.OfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/office")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    // body: { "parcelId": "...", "channel": "SMS", "message": "..." }
    @PostMapping("/notifications")
    public NotificationEntity notify(@RequestBody NotificationEntity n) {
        return officeService.notify(n.getParcelId(), n.getChannel(), n.getMessage());
    }
}
