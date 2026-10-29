package com.tirestore.dtdemoproject.repository;

import com.tirestore.dtdemoproject.entity.ParcelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ParcelRepository extends JpaRepository<ParcelEntity, UUID> {

    Optional<ParcelEntity> findByTrackingNum(String trackingNum);
}