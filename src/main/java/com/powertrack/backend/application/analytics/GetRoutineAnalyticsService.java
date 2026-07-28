package com.powertrack.backend.application.analytics;

import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase;
import com.powertrack.backend.application.analytics.port.out.AnalyticsRepositoryPort;
import com.powertrack.backend.application.routine.exception.RoutineNotFoundException;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.domain.analytics.ProgressClassifier;
import com.powertrack.backend.domain.analytics.ProgressStatus;
import com.powertrack.backend.domain.analytics.SessionAggregate;
import com.powertrack.backend.domain.analytics.SessionPerformanceAggregator;
import com.powertrack.backend.domain.analytics.SetSample;
import com.powertrack.backend.domain.routine.Exercise;
import com.powertrack.backend.domain.routine.Routine;
import com.powertrack.backend.domain.routine.RoutineDay;
import com.powertrack.backend.domain.routine.RoutineExercise;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetRoutineAnalyticsService implements GetRoutineAnalyticsUseCase {

    private static final long DEFAULT_WINDOW_DAYS = 30;

    /**
     * Se requieren al menos 2 sesiones históricas del mismo {@code routineExerciseId}
     * para poder comparar "última vs. anterior" con {@link ProgressClassifier}. Con
     * menos, el estado es {@link ProgressStatus#INSUFFICIENT_DATA} (decisión que
     * {@link ProgressClassifier} nunca toma por sí mismo).
     */
    private static final int REQUIRED_HISTORY_FOR_CLASSIFICATION = 2;

    private static final double SECONDS_PER_DAY = 86_400.0;

    private final RoutineRepositoryPort routineRepository;
    private final ExerciseRepositoryPort exerciseRepository;
    private final AnalyticsRepositoryPort analyticsRepository;

    public GetRoutineAnalyticsService(RoutineRepositoryPort routineRepository, ExerciseRepositoryPort exerciseRepository,
                                       AnalyticsRepositoryPort analyticsRepository) {
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public RoutineAnalyticsResult getRoutineAnalytics(UUID routineId, UUID userId, Instant from, Instant to) {
        // findByIdAndUserId filtra por dueño a nivel de query: si la rutina existe pero
        // es de otro usuario, no se encuentra y se trata igual que "no existe" (404),
        // mismo criterio que GetRoutineDetailService.
        Routine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RoutineNotFoundException(routineId));

        Instant effectiveTo = to != null ? to : Instant.now();
        Instant effectiveFrom = from != null ? from : effectiveTo.minus(DEFAULT_WINDOW_DAYS, ChronoUnit.DAYS);

        List<RoutineExercise> allRoutineExercises = routine.getDays().stream()
                .flatMap(day -> day.getExercises().stream())
                .toList();

        List<UUID> exerciseIds = allRoutineExercises.stream()
                .map(RoutineExercise::getExerciseId)
                .distinct()
                .toList();
        Map<UUID, Exercise> exercisesById = exerciseRepository.findAllByIds(exerciseIds);

        List<UUID> routineExerciseIds = allRoutineExercises.stream()
                .map(RoutineExercise::getId)
                .toList();
        Map<UUID, List<AnalyticsRepositoryPort.ExerciseSessionSets>> historyByRoutineExercise =
                analyticsRepository.findLastTwoSessionsPerRoutineExercise(routineExerciseIds, userId);

        BigDecimal totalVolumeKg = analyticsRepository.sumVolumeForRoutine(routineId, userId, effectiveFrom, effectiveTo);
        List<Instant> sessionEndTimes = analyticsRepository.findCompletedSessionEndTimesForRoutine(
                routineId, userId, effectiveFrom, effectiveTo);
        Double averageDaysBetweenSessions = computeAverageDaysBetween(sessionEndTimes);
        WindowResult window = new WindowResult(effectiveFrom, effectiveTo, sessionEndTimes.size(), averageDaysBetweenSessions);

        long progressed = 0;
        long maintained = 0;
        long regressed = 0;
        long insufficientData = 0;
        List<DayResult> byDay = new ArrayList<>();

        for (RoutineDay day : routine.getDays()) {
            List<ExerciseProgressResult> exerciseResults = new ArrayList<>();
            for (RoutineExercise routineExercise : day.getExercises()) {
                Exercise exercise = exercisesById.get(routineExercise.getExerciseId());
                String exerciseName = exercise != null ? exercise.getName() : null;
                String targetMuscle = exercise != null ? exercise.getTargetMuscle() : null;

                List<AnalyticsRepositoryPort.ExerciseSessionSets> history =
                        historyByRoutineExercise.getOrDefault(routineExercise.getId(), List.of());

                if (history.size() < REQUIRED_HISTORY_FOR_CLASSIFICATION) {
                    insufficientData++;
                    exerciseResults.add(new ExerciseProgressResult(routineExercise.getId(), routineExercise.getExerciseId(),
                            exerciseName, targetMuscle, ProgressStatus.INSUFFICIENT_DATA, null, null));
                    continue;
                }

                // history viene ordenado más reciente primero (contrato del puerto
                // AnalyticsRepositoryPort#findLastTwoSessionsPerRoutineExercise).
                AnalyticsRepositoryPort.ExerciseSessionSets lastSession = history.get(0);
                AnalyticsRepositoryPort.ExerciseSessionSets previousSession = history.get(1);

                SessionAggregate lastAggregate = SessionPerformanceAggregator.aggregate(toSamples(lastSession));
                SessionAggregate previousAggregate = SessionPerformanceAggregator.aggregate(toSamples(previousSession));

                ProgressStatus status = ProgressClassifier.classify(previousAggregate.referenceSet(), lastAggregate.referenceSet());
                switch (status) {
                    case PROGRESSED -> progressed++;
                    case MAINTAINED -> maintained++;
                    case REGRESSED -> regressed++;
                    case INSUFFICIENT_DATA -> insufficientData++; // ProgressClassifier nunca devuelve esto.
                }

                exerciseResults.add(new ExerciseProgressResult(routineExercise.getId(), routineExercise.getExerciseId(),
                        exerciseName, targetMuscle, status,
                        toSnapshot(lastSession, lastAggregate), toSnapshot(previousSession, previousAggregate)));
            }
            byDay.add(new DayResult(day.getId(), day.getDayName(), exerciseResults));
        }

        SummaryResult summary = new SummaryResult(progressed, maintained, regressed, insufficientData);
        return new RoutineAnalyticsResult(routine.getId(), routine.getName(), window, totalVolumeKg, summary, byDay);
    }

    private List<SetSample> toSamples(AnalyticsRepositoryPort.ExerciseSessionSets session) {
        return session.sets().stream()
                .map(s -> new SetSample(s.weightKg(), s.repsCompleted(), s.rpe()))
                .toList();
    }

    private SessionSnapshotResult toSnapshot(AnalyticsRepositoryPort.ExerciseSessionSets session, SessionAggregate aggregate) {
        ReferenceSetResult referenceSet = new ReferenceSetResult(aggregate.referenceSet().weightKg(),
                aggregate.referenceSet().repsCompleted(), aggregate.referenceSet().rpe());
        return new SessionSnapshotResult(session.sessionEndTime(), referenceSet);
    }

    private Double computeAverageDaysBetween(List<Instant> sortedAscendingEndTimes) {
        if (sortedAscendingEndTimes.size() < 2) {
            return null;
        }
        long totalSeconds = 0;
        for (int i = 1; i < sortedAscendingEndTimes.size(); i++) {
            totalSeconds += Duration.between(sortedAscendingEndTimes.get(i - 1), sortedAscendingEndTimes.get(i)).getSeconds();
        }
        int gaps = sortedAscendingEndTimes.size() - 1;
        return (totalSeconds / (double) gaps) / SECONDS_PER_DAY;
    }
}
