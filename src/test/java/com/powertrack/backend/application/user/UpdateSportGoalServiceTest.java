package com.powertrack.backend.application.user;

import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.application.user.exception.UserNotFoundException;
import com.powertrack.backend.application.user.port.in.UserProfileResult;
import com.powertrack.backend.domain.user.SportGoal;
import com.powertrack.backend.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSportGoalServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    private UpdateSportGoalService service;

    @BeforeEach
    void setUp() {
        service = new UpdateSportGoalService(userRepository);
    }

    @Test
    void actualizaElSportGoalYPersisteElUsuario() {
        User user = User.register("mateo@example.com", "hashed-password", "Mateo", SportGoal.POWERLIFTING);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserProfileResult result = service.updateSportGoal(user.getId(), SportGoal.HIPERTROFIA);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getId()).isEqualTo(user.getId());
        assertThat(savedUser.getSportGoal()).isEqualTo(SportGoal.HIPERTROFIA);
        assertThat(result.sportGoal()).isEqualTo(SportGoal.HIPERTROFIA);
        assertThat(result.email()).isEqualTo("mateo@example.com");
    }

    @Test
    void lanzaUserNotFoundSiElUsuarioNoExisteYNoGuardaNada() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateSportGoal(userId, SportGoal.HIPERTROFIA))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }
}
