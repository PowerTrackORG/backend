package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.user.port.in.UserProfileResult;
import com.powertrack.backend.domain.user.SportGoal;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String fullName, SportGoal sportGoal, Instant createdAt) {

    public static UserProfileResponse from(UserProfileResult result) {
        return new UserProfileResponse(result.id(), result.email(), result.fullName(), result.sportGoal(), result.createdAt());
    }
}
