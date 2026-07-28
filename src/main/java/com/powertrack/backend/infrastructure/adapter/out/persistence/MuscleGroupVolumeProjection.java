package com.powertrack.backend.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;

/**
 * Proyección JPQL (constructor expression) usada por
 * {@code WorkoutSessionJpaRepository#sumVolumeByMuscleGroup}: tonelaje total agregado
 * por grupo muscular, sin cargar entidades completas de sesión/log/serie.
 */
public class MuscleGroupVolumeProjection {

    private final String targetMuscle;
    private final BigDecimal totalVolumeKg;

    public MuscleGroupVolumeProjection(String targetMuscle, BigDecimal totalVolumeKg) {
        this.targetMuscle = targetMuscle;
        this.totalVolumeKg = totalVolumeKg;
    }

    public String getTargetMuscle() {
        return targetMuscle;
    }

    public BigDecimal getTotalVolumeKg() {
        return totalVolumeKg;
    }
}
