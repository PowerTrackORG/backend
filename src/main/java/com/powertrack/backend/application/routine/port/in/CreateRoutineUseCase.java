package com.powertrack.backend.application.routine.port.in;

import java.util.List;
import java.util.UUID;

public interface CreateRoutineUseCase {

    RoutineDetailResult create(CreateRoutineCommand command);

    record CreateRoutineCommand(UUID userId, String name, String description, List<RoutineDayCommand> days) {
    }

    record RoutineDayCommand(String dayName, int orderIndex, List<RoutineExerciseCommand> exercises) {
    }

    record RoutineExerciseCommand(UUID exerciseId, int orderIndex, int targetSets, int targetRepMin,
                                   int targetRepMax, Integer restSeconds, String notes) {
    }
}
