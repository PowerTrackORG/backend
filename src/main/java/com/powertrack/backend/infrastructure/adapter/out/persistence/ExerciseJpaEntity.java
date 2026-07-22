package com.powertrack.backend.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "exercises")
public class ExerciseJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_muscle", nullable = false)
    private String targetMuscle;

    @Column(nullable = false)
    private String category;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    protected ExerciseJpaEntity() {
        // requerido por JPA
    }

    public ExerciseJpaEntity(UUID id, String name, String targetMuscle, String category, UUID createdByUserId) {
        this.id = id;
        this.name = name;
        this.targetMuscle = targetMuscle;
        this.category = category;
        this.createdByUserId = createdByUserId;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetMuscle() {
        return targetMuscle;
    }

    public String getCategory() {
        return category;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }
}
