package com.powertrack.backend.application.auth;

import com.powertrack.backend.application.auth.exception.InvalidCredentialsException;
import com.powertrack.backend.application.auth.port.in.AuthResult;
import com.powertrack.backend.application.auth.port.in.AuthenticateUserUseCase.LoginCommand;
import com.powertrack.backend.application.auth.port.out.PasswordHasherPort;
import com.powertrack.backend.application.auth.port.out.TokenProviderPort;
import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.domain.user.SportGoal;
import com.powertrack.backend.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenProviderPort tokenProvider;

    private AuthenticateUserService service;

    @BeforeEach
    void setUp() {
        service = new AuthenticateUserService(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    void autenticaConCredencialesValidasYDevuelveTokens() {
        User user = User.register("mateo@example.com", "hashed-password", "Mateo", SportGoal.POWERLIFTING);
        when(userRepository.findByEmail("mateo@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("raw-password", "hashed-password")).thenReturn(true);
        when(tokenProvider.generateAccessToken(user)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResult result = service.authenticate(new LoginCommand("mateo@example.com", "raw-password"));

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.userId()).isEqualTo(user.getId());
    }

    @Test
    void rechazaEmailInexistenteConMensajeGenerico() {
        when(userRepository.findByEmail("no-existe@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(new LoginCommand("no-existe@example.com", "cualquier-password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rechazaPasswordIncorrectaConElMismoMensajeGenerico() {
        User user = User.register("mateo@example.com", "hashed-password", "Mateo", SportGoal.POWERLIFTING);
        when(userRepository.findByEmail("mateo@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("password-incorrecta", "hashed-password")).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(new LoginCommand("mateo@example.com", "password-incorrecta")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
