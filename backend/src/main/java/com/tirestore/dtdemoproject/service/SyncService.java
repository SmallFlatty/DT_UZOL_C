package com.tirestore.dtdemoproject.service;

import com.tirestore.dtdemoproject.dto.SyncEvent;
import com.tirestore.dtdemoproject.entity.DeletedParcelEntity;
import com.tirestore.dtdemoproject.entity.NotificationEntity;
import com.tirestore.dtdemoproject.entity.ParcelEntity;
import com.tirestore.dtdemoproject.entity.ProcessedEventEntity;
import com.tirestore.dtdemoproject.repository.DeletedParcelRepository;
import com.tirestore.dtdemoproject.repository.NotificationRepository;
import com.tirestore.dtdemoproject.repository.ParcelRepository;
import com.tirestore.dtdemoproject.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.UUID;

// spracuje zmeny od inych uzlov, do outboxu nic nepise
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final ParcelRepository parcelRepository;
    private final DeletedParcelRepository deletedParcelRepository;
    private final NotificationRepository notificationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    // jedna transakcia: zmena + zapis eventId -> bud sa stalo oboje, alebo nic
    @Transactional
    public void apply(SyncEvent event) {
        UUID eventId = event.eventId();

        // idempotencia: event, ktory sme uz aplikovali (napr. odpoved sa stratila a odosielatel opakuje)
        if (eventId != null && processedEventRepository.existsById(eventId)) {
            log.info("SYNC duplicate event {} ({} {}) - ignored", eventId, event.entity(), event.operation());
            return;
        }

        if ("PARCEL".equals(event.entity())) {
            if ("DELETE".equals(event.operation())) {
                deleteParcel(event.entityId());
            } else {
                saveParcel(event.payload()); // CREATE aj UPDATE
            }
        } else if ("NOTIFICATION".equals(event.entity())) {
            NotificationEntity n = objectMapper.readValue(event.payload(), NotificationEntity.class);
            notificationRepository.save(n);
        } else {
            // neznama entita: potvrdime, inak by zablokovala celu frontu pre daneho peera
            log.warn("SYNC unknown entity '{}' (event {}) - ignored", event.entity(), eventId);
        }

        if (eventId != null) {
            processedEventRepository.save(new ProcessedEventEntity(eventId, OffsetDateTime.now()));
        }
        log.info("SYNC applied {} {} {} (event {})", event.entity(), event.operation(), event.entityId(), eventId);
    }

    private void saveParcel(String payload) {
        ParcelEntity incoming = objectMapper.readValue(payload, ParcelEntity.class);

        // uz bola zmazana -> ignorovat
        if (deletedParcelRepository.existsById(incoming.getId())) {
            return;
        }

        // lokalna verzia je novsia -> ignorovat
        ParcelEntity local = parcelRepository.findById(incoming.getId()).orElse(null);
        if (local != null && !incoming.getUpdatedAt().isAfter(local.getUpdatedAt())) {
            return;
        }

        parcelRepository.save(incoming);
    }

    private void deleteParcel(UUID id) {
        parcelRepository.deleteById(id);

        // tombstone zapiseme len raz (opakovany DELETE nemeni deleted_at)
        if (!deletedParcelRepository.existsById(id)) {
            DeletedParcelEntity deleted = new DeletedParcelEntity();
            deleted.setParcelId(id);
            deleted.setDeletedAt(OffsetDateTime.now());
            deletedParcelRepository.save(deleted);
        }
    }
}
