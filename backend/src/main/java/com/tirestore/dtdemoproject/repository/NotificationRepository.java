package com.tirestore.dtdemoproject.repository;

import com.tirestore.dtdemoproject.entity.DeletedParcelEntity;
import com.tirestore.dtdemoproject.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByParcelId(UUID parcelId);
}
