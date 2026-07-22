package com.powertrack.backend.application.user;

import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.application.user.exception.UserNotFoundException;
import com.powertrack.backend.application.user.port.in.UserProfileResult;
import com.powertrack.backend.domain.user.SportGoal;
import com.powertrack.backend.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserProfileServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    private GetUserProfileService service;

    @BeforeEach
    void setUp() {
        service = new GetUserProfileService(userRepository);
    }

    @Test
    void devuelveElPerfilDelUsuarioAutenticado() {
        User user = User.register("mateo@example.com", "hashed-password", "Mateo", SportGoal.POWERLIFTING);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserProfileResult result = service.getProfile(user.getId());

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.email()).isEqualTo("mateo@example.com");
        assertThat(result.fullName()).isEqualTo("Mateo");
        assertThat(result.sportGoal()).isEqualTo(SportGoal.POWERLIFTING);
        assertThat(result.createdAt()).isEqualTo(user.getCreatedAt());
    }

    @Test
    void lanzaUserNotFoundSiElUsuarioNoExiste() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProfile(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
