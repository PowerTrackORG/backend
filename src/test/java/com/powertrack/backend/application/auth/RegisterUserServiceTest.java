package com.powertrack.backend.application.auth;

import com.powertrack.backend.application.auth.exception.EmailAlreadyRegisteredException;
import com.powertrack.backend.application.auth.port.in.AuthResult;
import com.powertrack.backend.application.auth.port.in.RegisterUserUseCase.RegisterCommand;
import com.powertrack.backend.application.auth.port.out.PasswordHasherPort;
import com.powertrack.backend.application.auth.port.out.TokenProviderPort;
import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.domain.user.SportGoal;
import com.powertrack.backend.domain.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private TokenProviderPort tokenProvider;

    private RegisterUserService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new RegisterUserService(userRepository, passwordHasher, tokenProvider);
    }

    @Test
    void registraUnUsuarioNuevoConPasswordHasheadaYDevuelveTokens() {
        RegisterCommand command = new RegisterCommand("mateo@example.com", "raw-password", "Mateo", SportGoal.POWERLIFTING);

        when(userRepository.existsByEmail("mateo@example.com")).thenReturn(false);
        when(passwordHasher.hash("raw-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenProvider.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        AuthResult result = service.register(command);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("mateo@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getSportGoal()).isEqualTo(SportGoal.POWERLIFTING);
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.email()).isEqualTo("mateo@example.com");
    }

    @Test
    void rechazaRegistroConEmailYaExistenteYNoGuardaNada() {
        RegisterCommand command = new RegisterCommand("mateo@example.com", "raw-password", "Mateo", SportGoal.POWERLIFTING);
        when(userRepository.existsByEmail("mateo@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
    }
}
