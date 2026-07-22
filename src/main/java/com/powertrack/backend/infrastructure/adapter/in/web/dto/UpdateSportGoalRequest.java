package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.domain.user.SportGoal;
import jakarta.validation.constraints.NotNull;

public record UpdateSportGoalRequest(@NotNull SportGoal sportGoal) {
}
