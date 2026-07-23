package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.workout.port.in.StartWorkoutSessionUseCase.StartSessionCommand;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * {@code sessionId} y {@code startTime} los genera/reporta el cliente (no el servidor) —
 * ver {@code Docs/decisiones-tecnicas.md}, entrada 2026-07-23. Sin validación de rango
 * sobre {@code startTime}: se toma la hora del dispositivo tal cual, decisión explícita
 * de producto.
 */
public record StartWorkoutSessionRequest(@NotNull UUID sessionId, @NotNull UUID routineDayId, @NotNull Instant startTime) {

    public StartSessionCommand toCommand(UUID userId) {
        return new StartSessionCommand(sessionId, userId, routineDayId, startTime);
    }
}
