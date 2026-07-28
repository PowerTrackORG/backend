package com.powertrack.backend.application.analytics;

import com.powertrack.backend.application.analytics.port.in.GetMuscleGroupVolumeUseCase;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class GetMuscleGroupVolumeService implements GetMuscleGroupVolumeUseCase {

    private static final long DEFAULT_WINDOW_DAYS = 7;

    private final AnalyticsRepositoryPort analyticsRepository;

    public GetMuscleGroupVolumeService(AnalyticsRepositoryPort analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public List<MuscleGroupVolumeResult> getMuscleGroupVolume(UUID userId, Instant from, Instant to) {
        Instant effectiveTo = to != null ? to : Instant.now();
        Instant effectiveFrom = from != null ? from : effectiveTo.minus(DEFAULT_WINDOW_DAYS, ChronoUnit.DAYS);

        return analyticsRepository.sumVolumeByMuscleGroup(userId, effectiveFrom, effectiveTo).stream()
                .map(v -> new MuscleGroupVolumeResult(v.targetMuscle(), v.totalVolumeKg()))
                .toList();
    }
}
