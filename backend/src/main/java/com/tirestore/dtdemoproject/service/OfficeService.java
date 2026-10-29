package com.tirestore.dtdemoproject.service;

import com.tirestore.dtdemoproject.config.NodeProperties;
import com.tirestore.dtdemoproject.config.NodeRole;
import com.tirestore.dtdemoproject.entity.NotificationEntity;
import com.tirestore.dtdemoproject.repository.NotificationRepository;
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
public class OfficeService {

    private final ParcelRepository parcelRepository;
    private final NotificationRepository notificationRepository;
    private final OutboxService outboxService;
    private final NodeProperties node;

    @Transactional
    public NotificationEntity notify(UUID parcelId, String channel, String message) {
        node.requireRole(NodeRole.OFFICE);

        if (!parcelRepository.existsById(parcelId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Zasielka neexistuje");
        }

        NotificationEntity n = new NotificationEntity();
        n.setId(UUID.randomUUID());
        n.setParcelId(parcelId);
        n.setChannel(channel);
        n.setMessage(message);
        n.setCreatedAt(OffsetDateTime.now());

        notificationRepository.save(n);
        outboxService.add("NOTIFICATION", "CREATE", n.getId(), n);
        return n;
    }
}
