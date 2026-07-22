package com.powertrack.backend.domain.workout;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework.
 * Vive dentro de un {@link WorkoutLog} como parte de una lista ordenada de series.
 */
public final class LogSet {

    private final UUID id;
    private final int setNumber;
    private final BigDecimal weightKg;
    private final int repsCompleted;
    private final int rpe;

    private LogSet(UUID id, int setNumber, BigDecimal weightKg, int repsCompleted, int rpe) {
        Objects.requireNonNull(weightKg, "weightKg es obligatorio");
        if (setNumber < 1) {
            throw new IllegalArgumentException("setNumber debe ser al menos 1");
        }
        if (weightKg.signum() < 0) {
            throw new IllegalArgumentException("weightKg no puede ser negativo");
        }
        if (repsCompleted < 1) {
            throw new IllegalArgumentException("repsCompleted debe ser mayor a 0");
        }
        if (rpe < 1 || rpe > 10) {
            throw new IllegalArgumentException("rpe debe estar entre 1 y 10");
        }
        this.id = id;
        this.setNumber = setNumber;
        this.weightKg = weightKg;
        this.repsCompleted = repsCompleted;
        this.rpe = rpe;
    }

    public static LogSet create(int setNumber, BigDecimal weightKg, int repsCompleted, int rpe) {
        return new LogSet(UUID.randomUUID(), setNumber, weightKg, repsCompleted, rpe);
    }

    public static LogSet rehydrate(UUID id, int setNumber, BigDecimal weightKg, int repsCompleted, int rpe) {
        return new LogSet(id, setNumber, weightKg, repsCompleted, rpe);
    }

    public UUID getId() {
        return id;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public int getRepsCompleted() {
        return repsCompleted;
    }

    public int getRpe() {
        return rpe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogSet logSet)) return false;
        return Objects.equals(id, logSet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
