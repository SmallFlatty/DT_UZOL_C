package com.tirestore.dtdemoproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "target",nullable = false)
    private String target;
    @Column(name = "entity",nullable = false)
    private String entity;
    @Column(name = "operation",nullable = false)
    private String operation;
    @Column(name = "entity_id",nullable = false)
    private UUID entityId;
    // rovnaky pre vsetky peer-riadky jednej operacie; nullable kvoli starsim riadkom v existujucich DB
    @Column(name = "event_id")
    private UUID eventId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload")
    private String payload;
    @Column(name ="created_at",nullable = false)
    private OffsetDateTime createdAt;
}