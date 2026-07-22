package com.powertrack.backend.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

/**
 * Proyección JPQL (constructor expression) usada por {@code findSummariesByUserId} para
 * traer solo lo que la pantalla de listado necesita (RNF-04), sin cargar días/ejercicios
 * completos de cada rutina.
 */
public class RoutineSummaryProjection {

    private final UUID id;
    private final String name;
    private final String description;
    private final Instant createdAt;
    private final long dayCount;

    public RoutineSummaryProjection(UUID id, String name, String description, Instant createdAt, long dayCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.dayCount = dayCount;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getDayCount() {
        return dayCount;
    }
}
