package com.powertrack.backend.application.auth;

import com.powertrack.backend.application.auth.exception.InvalidCredentialsException;
import com.powertrack.backend.application.auth.port.in.AuthResult;
import com.powertrack.backend.application.auth.port.in.AuthenticateUserUseCase;
import com.powertrack.backend.application.auth.port.out.PasswordHasherPort;
import com.powertrack.backend.application.auth.port.out.TokenProviderPort;
import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.domain.user.User;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final TokenProviderPort tokenProvider;

    public AuthenticateUserService(UserRepositoryPort userRepository, PasswordHasherPort passwordHasher, TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult authenticate(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        return new AuthResult(user.getId(), user.getEmail(), accessToken, refreshToken);
    }
}
