package com.powertrack.backend.application.auth.port.in;

public interface AuthenticateUserUseCase {

    AuthResult authenticate(LoginCommand command);

    record LoginCommand(String email, String rawPassword) {
    }
}
