package com.powertrack.backend.application.routine.port.in;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListRoutinesUseCase {

    List<RoutineSummaryResult> list(UUID userId);

    record RoutineSummaryResult(UUID id, String name, String description, Instant createdAt, int dayCount) {
    }
}
