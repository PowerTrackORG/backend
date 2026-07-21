package com.powertrack.backend.application.auth.port.in;

import com.powertrack.backend.domain.user.SportGoal;

public interface RegisterUserUseCase {

    AuthResult register(RegisterCommand command);

    record RegisterCommand(String email, String rawPassword, String fullName, SportGoal sportGoal) {
    }
}
