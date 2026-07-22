package com.powertrack.backend.application.routine.port.in;

import java.util.UUID;

public record ExerciseResult(UUID id, String name, String targetMuscle, String category, boolean predefined) {
}
