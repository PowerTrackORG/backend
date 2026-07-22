package com.powertrack.backend.application.routine.port.in;

import java.util.List;
import java.util.UUID;

public interface ListExercisesUseCase {

    List<ExerciseResult> list(UUID userId);
}
