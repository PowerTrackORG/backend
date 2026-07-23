package com.powertrack.backend.domain.workout;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework.
 * <p>
 * Agregado raíz: expone una lista inmutable de {@link WorkoutLog}, cada uno con su propia
 * lista inmutable de {@link LogSet}, siguiendo la misma convención de agregado que
 * {@code Routine -&gt; RoutineDay -&gt; RoutineExercise}.
 */
public final class WorkoutSession {

    private final UUID id;
    private final UUID userId;
    private final UUID routineDayId;
    private final Instant startTime;
    private final Instant endTime;
    private final SessionStatus status;
    private final OverallFeeling overallFeeling;
    private final List<WorkoutLog> logs;

    private WorkoutSession(UUID id, UUID userId, UUID routineDayId, Instant startTime, Instant endTime,
                            SessionStatus status, OverallFeeling overallFeeling, List<WorkoutLog> logs) {
        Objects.requireNonNull(userId, "userId es obligatorio");
        Objects.requireNonNull(routineDayId, "routineDayId es obligatorio");
        Objects.requireNonNull(startTime, "startTime es obligatorio");
        Objects.requireNonNull(status, "status es obligatorio");
        Objects.requireNonNull(logs, "logs es obligatorio");
        if (status == SessionStatus.COMPLETED && (endTime == null || overallFeeling == null)) {
            throw new IllegalArgumentException("una sesión COMPLETED requiere endTime y overallFeeling");
        }
        if (status == SessionStatus.IN_PROGRESS && (endTime != null || overallFeeling != null)) {
            throw new IllegalArgumentException("una sesión IN_PROGRESS no puede tener endTime ni overallFeeling");
        }
        this.id = id;
        this.userId = userId;
        this.routineDayId = routineDayId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.overallFeeling = overallFeeling;
        this.logs = List.copyOf(logs);
    }

    /**
     * {@code id} y {@code startTime} los genera/reporta el cliente (no el servidor): así
     * iniciar una sesión es idempotente al sincronizar desde modo offline, y la fecha
     * refleja cuándo se entrenó realmente, no cuándo llegó la sincronización. Ver
     * {@code Docs/decisiones-tecnicas.md}, entrada 2026-07-23.
     */
    public static WorkoutSession start(UUID id, UUID userId, UUID routineDayId, Instant startTime) {
        return new WorkoutSession(id, userId, routineDayId, startTime, null,
                SessionStatus.IN_PROGRESS, null, List.of());
    }

    public static WorkoutSession rehydrate(UUID id, UUID userId, UUID routineDayId, Instant startTime, Instant endTime,
                                            SessionStatus status, OverallFeeling overallFeeling, List<WorkoutLog> logs) {
        return new WorkoutSession(id, userId, routineDayId, startTime, endTime, status, overallFeeling, logs);
    }

    /**
     * Transiciona la sesión a COMPLETED junto con los logs de ejercicios registrados.
     * <p>
     * Solo permite finalizar una sesión IN_PROGRESS (lanza {@link IllegalStateException}
     * en cualquier otro caso, como red de seguridad). La validación de negocio de "la
     * sesión ya está finalizada" (idempotencia del endpoint, con su propio código HTTP)
     * es responsabilidad de la capa de aplicación
     * ({@code FinishWorkoutSessionService}), que debe comprobar el estado ANTES de
     * invocar este método.
     */
    public WorkoutSession finish(OverallFeeling overallFeeling, List<WorkoutLog> logs, Instant endTime) {
        if (status != SessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Solo una sesión IN_PROGRESS puede finalizarse");
        }
        Objects.requireNonNull(overallFeeling, "overallFeeling es obligatorio al finalizar la sesión");
        Objects.requireNonNull(endTime, "endTime es obligatorio al finalizar la sesión");
        return new WorkoutSession(id, userId, routineDayId, startTime, endTime, SessionStatus.COMPLETED,
                overallFeeling, logs);
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoutineDayId() {
        return routineDayId;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public OverallFeeling getOverallFeeling() {
        return overallFeeling;
    }

    public List<WorkoutLog> getLogs() {
        return logs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkoutSession that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
