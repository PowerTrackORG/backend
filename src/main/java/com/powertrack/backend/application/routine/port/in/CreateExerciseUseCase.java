package com.powertrack.backend.application.routine.port.in;

import java.util.UUID;

public interface CreateExerciseUseCase {

    ExerciseResult create(CreateExerciseCommand command);

    record CreateExerciseCommand(UUID userId, String name, String targetMuscle, String category) {
    }
}
