package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateExerciseRequest(
        @NotBlank String name,
        @NotBlank String targetMuscle,
        @NotBlank String category
) {
}
