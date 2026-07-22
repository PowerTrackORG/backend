package com.powertrack.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "routines")
public class RoutineJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Nota: se usa Set (no List) deliberadamente. Hibernate no permite hacer fetch join de
    // dos colecciones "bag" (List sin @OrderColumn) simultáneamente en la misma consulta
    // (MultipleBagFetchException) cuando se cargan "days.exercises" vía @EntityGraph, ya que
    // ambos niveles serían listas. Al convertir este nivel a Set se resuelve la ambigüedad;
    // el orden real se sigue garantizando con @OrderBy y, además, se reordena
    // defensivamente en RoutinePersistenceAdapter al mapear a dominio.
    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private Set<RoutineDayJpaEntity> days = new LinkedHashSet<>();

    protected RoutineJpaEntity() {
        // requerido por JPA
    }

    public RoutineJpaEntity(UUID id, UUID userId, String name, String description, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    public void replaceDays(List<RoutineDayJpaEntity> newDays) {
        this.days.clear();
        this.days.addAll(newDays);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
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

    public Set<RoutineDayJpaEntity> getDays() {
        return days;
    }
}
