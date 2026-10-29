package com.tirestore.dtdemoproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "deleted_parcels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeletedParcelEntity {

    @Id
    @Column(name = "parcel_id")
    private UUID parcelId;

    @Column(name = "deleted_at", nullable = false)
    private OffsetDateTime deletedAt;

}