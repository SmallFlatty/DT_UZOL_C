package com.tirestore.dtdemoproject.service;

import com.tirestore.dtdemoproject.config.NodeProperties;
import com.tirestore.dtdemoproject.config.NodeRole;
import com.tirestore.dtdemoproject.entity.ParcelEntity;
import com.tirestore.dtdemoproject.entity.ParcelStatus;
import com.tirestore.dtdemoproject.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepotService {

    private final ParcelRepository parcelRepository;
    private final OutboxService outboxService;
    private final NodeProperties node;

    @Transactional
    public ParcelEntity changeStatus(UUID id, ParcelStatus status) {
        node.requireRole(NodeRole.DEPOT);

        ParcelEntity p = parcelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zasielka neexistuje"));

        p.setStatus(status);
        p.setUpdatedAt(OffsetDateTime.now());

        parcelRepository.save(p);
        outboxService.add("PARCEL", "UPDATE", p.getId(), p);
        return p;
    }
}
