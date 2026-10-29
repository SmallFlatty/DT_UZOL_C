package com.tirestore.dtdemoproject.service;

import com.tirestore.dtdemoproject.config.NodeProperties;
import com.tirestore.dtdemoproject.config.NodeRole;
import com.tirestore.dtdemoproject.entity.DeletedParcelEntity;
import com.tirestore.dtdemoproject.entity.ParcelEntity;
import com.tirestore.dtdemoproject.entity.ParcelStatus;
import com.tirestore.dtdemoproject.repository.DeletedParcelRepository;
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
public class PointService {

    private final ParcelRepository parcelRepository;
    private final DeletedParcelRepository deletedParcelRepository;
    private final OutboxService outboxService;
    private final NodeProperties node;

    @Transactional
    public ParcelEntity create(ParcelEntity input) {
        node.requireRole(NodeRole.POINT);

        ParcelEntity p = new ParcelEntity();

        // od klienta
        p.setSenderName(input.getSenderName());
        p.setSenderPhone(input.getSenderPhone());
        p.setRecipientName(input.getRecipientName());
        p.setRecipientPhone(input.getRecipientPhone());
        p.setWeight(input.getWeight());

        // systemove
        p.setId(UUID.randomUUID());
        p.setTrackingNum("DE-" + node.code() + "-" + UUID.randomUUID().toString().substring(0, 8));
        p.setStatus(ParcelStatus.PRIJATA);
        p.setCreatedNode(node.code());
        p.setCreatedAt(OffsetDateTime.now());
        p.setUpdatedAt(OffsetDateTime.now());

        parcelRepository.save(p);
        outboxService.add("PARCEL", "CREATE", p.getId(), p);
        return p;
    }

    @Transactional
    public void handOver(UUID id) {
        node.requireRole(NodeRole.POINT);
        ParcelEntity p = findParcel(id);

        if (p.getStatus() != ParcelStatus.PRIPRAVENA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Zasielka nie je pripravena");
        }
        delete(p);
    }

    @Transactional
    public void storno(UUID id) {
        node.requireRole(NodeRole.POINT);
        delete(findParcel(id));
    }

    private void delete(ParcelEntity p) {
        parcelRepository.delete(p);

        DeletedParcelEntity deleted = new DeletedParcelEntity();
        deleted.setParcelId(p.getId());
        deleted.setDeletedAt(OffsetDateTime.now());
        deletedParcelRepository.save(deleted);

        outboxService.add("PARCEL", "DELETE", p.getId(), null);
    }

    private ParcelEntity findParcel(UUID id) {
        return parcelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zasielka neexistuje"));
    }
}
