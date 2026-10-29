package com.tirestore.dtdemoproject.controller;

import com.tirestore.dtdemoproject.entity.ParcelEntity;
import com.tirestore.dtdemoproject.entity.ParcelStatus;
import com.tirestore.dtdemoproject.service.DepotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/depot")
@RequiredArgsConstructor
public class DepotController {

    private final DepotService depotService;

    // PUT /api/depot/parcels/{id}/status?status=NA_DEPE
    @PutMapping("/parcels/{id}/status")
    public ParcelEntity changeStatus(@PathVariable UUID id, @RequestParam ParcelStatus status) {
        return depotService.changeStatus(id, status);
    }
}
