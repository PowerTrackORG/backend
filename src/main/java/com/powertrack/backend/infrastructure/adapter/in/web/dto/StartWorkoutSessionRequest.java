package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record StartWorkoutSessionRequest(@NotNull UUID routineDayId) {
}
