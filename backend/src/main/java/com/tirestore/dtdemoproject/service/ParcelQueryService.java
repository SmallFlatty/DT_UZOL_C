package com.tirestore.dtdemoproject.service;

import com.tirestore.dtdemoproject.entity.NotificationEntity;
import com.tirestore.dtdemoproject.entity.ParcelEntity;
import com.tirestore.dtdemoproject.repository.NotificationRepository;
import com.tirestore.dtdemoproject.repository.ParcelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParcelQueryService {

    private final ParcelRepository parcelRepository;
    private final NotificationRepository notificationRepository;

    public List<ParcelEntity> findAll() {
        return parcelRepository.findAll();
    }

    public ParcelEntity findByTrackingNum(String trackingNum) {
        return parcelRepository.findByTrackingNum(trackingNum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zasielka neexistuje"));
    }

    public List<NotificationEntity> findNotifications(UUID parcelId) {
        return notificationRepository.findByParcelId(parcelId);
    }
}
