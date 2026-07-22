package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.exception.RoutineNotFoundException;
import com.powertrack.backend.application.routine.port.in.RoutineDetailResult;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.domain.routine.Exercise;
import com.powertrack.backend.domain.routine.Routine;
import com.powertrack.backend.domain.routine.RoutineDay;
import com.powertrack.backend.domain.routine.RoutineExercise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRoutineDetailServiceTest {

    @Mock
    private RoutineRepositoryPort routineRepository;
    @Mock
    private ExerciseRepositoryPort exerciseRepository;

    private GetRoutineDetailService service;

    @BeforeEach
    void setUp() {
        service = new GetRoutineDetailService(routineRepository, exerciseRepository);
    }

    @Test
    void devuelveElDetalleCompletoCuandoLaRutinaPerteneceAlUsuarioAutenticado() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        RoutineExercise routineExercise = RoutineExercise.create(exerciseId, 0, 4, 8, 12, 90, null);
        RoutineDay day = RoutineDay.create("Día A", 0, List.of(routineExercise));
        Routine routine = Routine.rehydrate(routineId, userId, "Heavy Duty", "desc", java.time.Instant.now(), List.of(day));
        Exercise exercise = Exercise.rehydrate(exerciseId, "Sentadilla", "Piernas", "Empuje", null);

        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.of(routine));
        when(exerciseRepository.findAllByIds(List.of(exerciseId))).thenReturn(Map.of(exerciseId, exercise));

        RoutineDetailResult result = service.getDetail(routineId, userId);

        assertThat(result.id()).isEqualTo(routineId);
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.days()).hasSize(1);
        assertThat(result.days().get(0).exercises().get(0).exerciseName()).isEqualTo("Sentadilla");
    }

    @Test
    void lanzaRoutineNotFoundCuandoLaRutinaNoExisteOPerteneceAOtroUsuario() {
        UUID userId = UUID.randomUUID();
        UUID routineId = UUID.randomUUID();

        when(routineRepository.findByIdAndUserId(routineId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(routineId, userId))
                .isInstanceOf(RoutineNotFoundException.class);
    }
}
