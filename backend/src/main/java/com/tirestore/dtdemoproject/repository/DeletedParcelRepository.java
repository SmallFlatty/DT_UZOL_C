package com.tirestore.dtdemoproject.repository;

import com.tirestore.dtdemoproject.entity.DeletedParcelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeletedParcelRepository extends JpaRepository<DeletedParcelEntity, UUID> {
}
