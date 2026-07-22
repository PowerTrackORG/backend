package com.powertrack.backend.application.workout;

import com.powertrack.backend.application.workout.port.in.GetPreviousLogUseCase.PreviousLogResult;
import com.powertrack.backend.application.workout.port.out.WorkoutLogRepositoryPort;
import com.powertrack.backend.application.workout.port.out.WorkoutLogRepositoryPort.LogSetData;
import com.powertrack.backend.application.workout.port.out.WorkoutLogRepositoryPort.PreviousLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPreviousLogServiceTest {

    @Mock
    private WorkoutLogRepositoryPort workoutLogRepository;

    private GetPreviousLogService service;

    @BeforeEach
    void setUp() {
        service = new GetPreviousLogService(workoutLogRepository);
    }

    @Test
    void devuelveElUltimoLogConSusSeriesCuandoHayHistorico() {
        UUID userId = UUID.randomUUID();
        UUID routineExerciseId = UUID.randomUUID();
        UUID workoutLogId = UUID.randomUUID();
        Instant performedAt = Instant.now();

        PreviousLog previousLog = new PreviousLog(workoutLogId, performedAt, "buena técnica",
                List.of(new LogSetData(1, BigDecimal.valueOf(100), 10, 8)));

        when(workoutLogRepository.findLatestByRoutineExerciseIdAndUserId(routineExerciseId, userId))
                .thenReturn(Optional.of(previousLog));

        Optional<PreviousLogResult> result = service.getPrevious(routineExerciseId, userId);

        assertThat(result).isPresent();
        assertThat(result.get().workoutLogId()).isEqualTo(workoutLogId);
        assertThat(result.get().routineExerciseId()).isEqualTo(routineExerciseId);
        assertThat(result.get().sets()).hasSize(1);
        assertThat(result.get().sets().get(0).weightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void devuelveVacioCuandoNoHayHistoricoYNoEsUnError() {
        UUID userId = UUID.randomUUID();
        UUID routineExerciseId = UUID.randomUUID();

        when(workoutLogRepository.findLatestByRoutineExerciseIdAndUserId(routineExerciseId, userId))
                .thenReturn(Optional.empty());

        Optional<PreviousLogResult> result = service.getPrevious(routineExerciseId, userId);

        assertThat(result).isEmpty();
    }
}
