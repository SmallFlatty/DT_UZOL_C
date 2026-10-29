package com.tirestore.dtdemoproject.repository;

import com.tirestore.dtdemoproject.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {

    // kolko operacii caka na odoslanie konkretnemu peerovi
    long countByTarget(String target);
}
