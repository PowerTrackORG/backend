package com.powertrack.backend.application.auth;

import com.powertrack.backend.application.auth.exception.EmailAlreadyRegisteredException;
import com.powertrack.backend.application.auth.port.in.AuthResult;
import com.powertrack.backend.application.auth.port.in.RegisterUserUseCase;
import com.powertrack.backend.application.auth.port.out.PasswordHasherPort;
import com.powertrack.backend.application.auth.port.out.TokenProviderPort;
import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public RegisterUserService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyRegisteredException(command.email());
        }

        String passwordHash = passwordHasher.hash(command.rawPassword());
        User user = User.register(command.email(), passwordHash, command.fullName(), command.sportGoal());
        User saved = userRepository.save(user);

        String accessToken = tokenProvider.generateAccessToken(saved);
        String refreshToken = tokenProvider.generateRefreshToken(saved);
        return new AuthResult(saved.getId(), saved.getEmail(), accessToken, refreshToken);
    }
}
