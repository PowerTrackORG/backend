package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.routine.port.in.ListRoutinesUseCase.RoutineSummaryResult;

import java.time.Instant;
import java.util.UUID;

public record RoutineSummaryResponse(UUID id, String name, String description, Instant createdAt, int dayCount) {

    public static RoutineSummaryResponse from(RoutineSummaryResult result) {
        return new RoutineSummaryResponse(result.id(), result.name(), result.description(), result.createdAt(), result.dayCount());
    }
}
