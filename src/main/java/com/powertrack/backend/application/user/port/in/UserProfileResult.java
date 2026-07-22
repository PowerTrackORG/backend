package com.powertrack.backend.application.user.port.in;

import com.powertrack.backend.domain.user.SportGoal;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResult(UUID id, String email, String fullName, SportGoal sportGoal, Instant createdAt) {
}
