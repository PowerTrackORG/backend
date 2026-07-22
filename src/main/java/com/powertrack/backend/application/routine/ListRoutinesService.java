package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.port.in.ListRoutinesUseCase;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListRoutinesService implements ListRoutinesUseCase {

    private final RoutineRepositoryPort routineRepository;

    public ListRoutinesService(RoutineRepositoryPort routineRepository) {
        this.routineRepository = routineRepository;
    }

    @Override
    public List<RoutineSummaryResult> list(UUID userId) {
        return routineRepository.findSummariesByUserId(userId).stream()
                .map(summary -> new RoutineSummaryResult(summary.id(), summary.name(), summary.description(),
                        summary.createdAt(), summary.dayCount()))
                .toList();
    }
}
