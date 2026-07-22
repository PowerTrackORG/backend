package com.powertrack.backend.domain.workout;

/**
 * Estado de una {@link WorkoutSession}. Solo existe la transición IN_PROGRESS -&gt;
 * COMPLETED (ver {@link WorkoutSession#finish}); no hay cancelación en el MVP.
 */
public enum SessionStatus {
    IN_PROGRESS,
    COMPLETED
}
