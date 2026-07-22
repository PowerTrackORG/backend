package com.powertrack.backend.infrastructure.adapter.out.persistence;

import java.util.UUID;

/**
 * Proyección JPQL (constructor expression) usada por
 * {@code RoutineJpaRepository#findRoutineExerciseTargets}: trae solo los targets que el
 * módulo de Registro/Progreso necesita para evaluar el motor de sugerencias, sin cargar
 * el agregado completo de la rutina.
 */
public class RoutineExerciseTargetsProjection {

    private final UUID id;
    private final int targetSets;
    private final int targetRepMin;
    private final int targetRepMax;

    public RoutineExerciseTargetsProjection(UUID id, int targetSets, int targetRepMin, int targetRepMax) {
        this.id = id;
        this.targetSets = targetSets;
        this.targetRepMin = targetRepMin;
        this.targetRepMax = targetRepMax;
    }

    public UUID getId() {
        return id;
    }

    public int getTargetSets() {
        return targetSets;
    }

    public int getTargetRepMin() {
        return targetRepMin;
    }

    public int getTargetRepMax() {
        return targetRepMax;
    }
}
