package com.tirestore.dtdemoproject.controller;

import com.tirestore.dtdemoproject.entity.ParcelEntity;
import com.tirestore.dtdemoproject.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/parcels")
    public ParcelEntity create(@RequestBody ParcelEntity parcel) {
        return pointService.create(parcel);
    }

    @PostMapping("/parcels/{id}/hand-over")
    public void handOver(@PathVariable UUID id) {
        pointService.handOver(id);
    }

    @DeleteMapping("/parcels/{id}")
    public void storno(@PathVariable UUID id) {
        pointService.storno(id);
    }
}
