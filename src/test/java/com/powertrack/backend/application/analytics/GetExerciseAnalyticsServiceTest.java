package com.powertrack.backend.application.analytics;

import com.powertrack.backend.application.analytics.port.in.GetExerciseAnalyticsUseCase.ExerciseAnalyticsResult;
import com.powertrack.backend.application.analytics.port.in.GetExerciseAnalyticsUseCase.SessionPointResult;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort.ExerciseSessionSets;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort.SetData;
import com.powertrack.backend.application.routine.exception.ExerciseNotFoundException;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.domain.routine.Exercise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetExerciseAnalyticsServiceTest {

    @Mock
    private ExerciseRepositoryPort exerciseRepository;
    @Mock
    private AnalyticsRepositoryPort analyticsRepository;

    private GetExerciseAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new GetExerciseAnalyticsService(exerciseRepository, analyticsRepository);
    }

    @Test
    void lanza404YNoConsultaElPuertoDeAnalyticsCuandoElEjercicioNoExisteEnElCatalogo() {
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(exerciseRepository.findById(exerciseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExerciseAnalytics(exerciseId, userId, null, null))
                .isInstanceOf(ExerciseNotFoundException.class);

        verifyNoInteractions(analyticsRepository);
    }

    @Test
    void combinaCorrectamenteLasSeriesCuandoElAdapterYaLasEntregaCombinadasEnUnSoloPunto() {
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant sessionEndTime = Instant.now();
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(Exercise.rehydrate(exerciseId, "Press banca", "Pecho", "Empuje", null)));

        // El adaptador ya combinó las series de 2 routineExercise distintos del mismo
        // exerciseId dentro de la misma sesión en un solo ExerciseSessionSets: el
        // service no debe repetir esa lógica de combinación, solo agregarla.
        ExerciseSessionSets combinedSession = new ExerciseSessionSets(sessionId, sessionEndTime, List.of(
                new SetData(BigDecimal.valueOf(100), 8, 8),
                new SetData(BigDecimal.valueOf(90), 10, 7)));
        when(analyticsRepository.findCompletedSessionSetsForExercise(exerciseId, userId, from, to))
                .thenReturn(List.of(combinedSession));

        ExerciseAnalyticsResult result = service.getExerciseAnalytics(exerciseId, userId, from, to);

        assertThat(result.exerciseId()).isEqualTo(exerciseId);
        assertThat(result.points()).hasSize(1);
        SessionPointResult point = result.points().get(0);
        assertThat(point.sessionDate()).isEqualTo(sessionEndTime);
        // Volumen = 100*8 + 90*10 = 1700, sumando ambas series combinadas.
        assertThat(point.volumeKg()).isEqualByComparingTo(BigDecimal.valueOf(1700));
        // referenceSet: mejor e1RM entre las que califican -> e1RM(100,8)=126.67 vs
        // e1RM(90,10)=120.00 -> gana 100kg x8.
        assertThat(point.referenceSet().weightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(point.referenceSet().reps()).isEqualTo(8);
    }

    @Test
    void aplicaDefaultDeFromYToCuandoNoSePasanComoParametro() {
        UUID exerciseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(exerciseRepository.findById(exerciseId))
                .thenReturn(Optional.of(Exercise.rehydrate(exerciseId, "Sentadilla", "Piernas", "Empuje", null)));
        when(analyticsRepository.findCompletedSessionSetsForExercise(eq(exerciseId), eq(userId), any(), any()))
                .thenReturn(List.of());

        Instant before = Instant.now();
        service.getExerciseAnalytics(exerciseId, userId, null, null);
        Instant after = Instant.now();

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(analyticsRepository).findCompletedSessionSetsForExercise(eq(exerciseId), eq(userId),
                fromCaptor.capture(), toCaptor.capture());

        assertThat(fromCaptor.getValue()).isEqualTo(Instant.EPOCH);
        assertThat(toCaptor.getValue()).isBetween(before.minus(5, ChronoUnit.SECONDS), after.plus(5, ChronoUnit.SECONDS));
    }
}
