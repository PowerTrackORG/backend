package com.powertrack.backend.application.analytics;

import com.powertrack.backend.application.analytics.port.in.GetMuscleGroupVolumeUseCase.MuscleGroupVolumeResult;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort.MuscleGroupVolume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMuscleGroupVolumeServiceTest {

    @Mock
    private AnalyticsRepositoryPort analyticsRepository;

    private GetMuscleGroupVolumeService service;

    @BeforeEach
    void setUp() {
        service = new GetMuscleGroupVolumeService(analyticsRepository);
    }

    @Test
    void aplicaVentanaPorDefectoDeSieteDiasCuandoNoSePasanFromNiTo() {
        UUID userId = UUID.randomUUID();
        when(analyticsRepository.sumVolumeByMuscleGroup(eq(userId), any(), any())).thenReturn(List.of());

        service.getMuscleGroupVolume(userId, null, null);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(analyticsRepository).sumVolumeByMuscleGroup(eq(userId), fromCaptor.capture(), toCaptor.capture());

        Duration window = Duration.between(fromCaptor.getValue(), toCaptor.getValue());
        assertThat(window).isCloseTo(Duration.ofDays(7), Duration.ofSeconds(5));
    }

    @Test
    void devuelvePassThroughDelResultadoDelPuertoOrdenadoPorVolumenDescendente() {
        UUID userId = UUID.randomUUID();
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(analyticsRepository.sumVolumeByMuscleGroup(userId, from, to)).thenReturn(List.of(
                new MuscleGroupVolume("Pecho", BigDecimal.valueOf(5000)),
                new MuscleGroupVolume("Piernas", BigDecimal.valueOf(3000))));

        List<MuscleGroupVolumeResult> result = service.getMuscleGroupVolume(userId, from, to);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).targetMuscle()).isEqualTo("Pecho");
        assertThat(result.get(0).totalVolumeKg()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(result.get(1).targetMuscle()).isEqualTo("Piernas");
    }
}
