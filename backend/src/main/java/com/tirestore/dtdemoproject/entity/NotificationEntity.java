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
@Getter
@Setter
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "parcel_id",nullable = false)
    private UUID parcelId;

    @Column(name = "channel",nullable = false)
    private String channel;

    @Column(name = "message",nullable = false)
    private String message;

    @Column(name = "created_at",nullable = false)
    private OffsetDateTime createdAt;
}
