package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.routine.port.in.ExerciseResult;

import java.util.UUID;

public record ExerciseResponse(UUID id, String name, String targetMuscle, String category, boolean predefined) {

    public static ExerciseResponse from(ExerciseResult result) {
        return new ExerciseResponse(result.id(), result.name(), result.targetMuscle(), result.category(), result.predefined());
    }
}
