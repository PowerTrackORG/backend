package com.powertrack.backend.application.workout;

import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort.RoutineExerciseTargets;
import com.powertrack.backend.application.workout.exception.RoutineExerciseNotFoundException;
import com.powertrack.backend.application.workout.exception.WorkoutSessionAlreadyFinishedException;
import com.powertrack.backend.application.workout.exception.WorkoutSessionNotFoundException;
import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase;
import com.powertrack.backend.application.workout.port.out.WorkoutLogRepositoryPort;
import com.powertrack.backend.application.workout.port.out.WorkoutSessionRepositoryPort;
import com.powertrack.backend.domain.workout.ExerciseEvaluationInput;
import com.powertrack.backend.domain.workout.HistoricalExerciseSummary;
import com.powertrack.backend.domain.workout.LogSet;
import com.powertrack.backend.domain.workout.ProgressionRuleEngine;
import com.powertrack.backend.domain.workout.Recommendation;
import com.powertrack.backend.domain.workout.SessionStatus;
import com.powertrack.backend.domain.workout.WorkoutLog;
import com.powertrack.backend.domain.workout.WorkoutSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinishWorkoutSessionService implements FinishWorkoutSessionUseCase {

    /**
     * Se piden solo las 2 sesiones históricas más recientes (no 3): junto con el
     * desempeño de la sesión que se está finalizando (que cuenta como la 3ª y más
     * reciente, ver Javadoc de {@link ProgressionRuleEngine}), forman la ventana de 3
     * sesiones consecutivas que exige la Regla 5.
     */
    private static final int DELOAD_HISTORY_LOOKBACK = 2;

    private final WorkoutSessionRepositoryPort workoutSessionRepository;
    private final WorkoutLogRepositoryPort workoutLogRepository;
    private final RoutineRepositoryPort routineRepository;
    // ProgressionRuleEngine es una clase de dominio pura, sin anotaciones de Spring: se
    // instancia directamente en vez de registrarla como bean, para no filtrar
    // dependencias de framework hacia el núcleo de dominio.
    private final ProgressionRuleEngine ruleEngine = new ProgressionRuleEngine();

    public FinishWorkoutSessionService(WorkoutSessionRepositoryPort workoutSessionRepository,
                                        WorkoutLogRepositoryPort workoutLogRepository,
                                        RoutineRepositoryPort routineRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.workoutLogRepository = workoutLogRepository;
        this.routineRepository = routineRepository;
    }

    @Override
    public FinishSessionResult finish(FinishSessionCommand command) {
        WorkoutSession session = workoutSessionRepository.findByIdAndUserId(command.sessionId(), command.userId())
                .orElseThrow(() -> new WorkoutSessionNotFoundException(command.sessionId()));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new WorkoutSessionAlreadyFinishedException(command.sessionId());
        }

        List<WorkoutLog> logs = new ArrayList<>();
        List<ExerciseSuggestionResult> suggestions = new ArrayList<>();

        for (ExerciseLogCommand exerciseLogCommand : command.exerciseLogs()) {
            RoutineExerciseTargets targets = routineRepository
                    .findRoutineExerciseTargets(exerciseLogCommand.routineExerciseId(), command.userId())
                    .orElseThrow(() -> new RoutineExerciseNotFoundException(exerciseLogCommand.routineExerciseId()));

            List<LogSet> sets = exerciseLogCommand.sets().stream()
                    .map(s -> LogSet.create(s.setNumber(), s.weightKg(), s.repsCompleted(), s.rpe()))
                    .toList();
            WorkoutLog log = WorkoutLog.create(exerciseLogCommand.routineExerciseId(), exerciseLogCommand.notes(), sets);
            logs.add(log);

            // Histórico consultado ANTES de persistir la sesión actual: naturalmente
            // excluye esta misma sesión, porque todavía no existe como COMPLETED en la
            // base de datos en este punto del flujo.
            List<HistoricalExerciseSummary> history = workoutLogRepository.findRecentSessionSummaries(
                    exerciseLogCommand.routineExerciseId(), command.userId(), DELOAD_HISTORY_LOOKBACK);

            List<ExerciseEvaluationInput.SetPerformance> setPerformances = sets.stream()
                    .map(s -> new ExerciseEvaluationInput.SetPerformance(s.getWeightKg(), s.getRepsCompleted(), s.getRpe()))
                    .toList();

            ExerciseEvaluationInput input = new ExerciseEvaluationInput(targets.targetRepMin(), targets.targetRepMax(),
                    setPerformances, command.overallFeeling(), history);
            Recommendation recommendation = ruleEngine.evaluate(input);

            suggestions.add(new ExerciseSuggestionResult(exerciseLogCommand.routineExerciseId(), recommendation,
                    recommendation.defaultMessage()));
        }

        WorkoutSession finished = session.finish(command.overallFeeling(), logs);
        WorkoutSession saved = workoutSessionRepository.save(finished);

        return new FinishSessionResult(saved.getId(), saved.getStartTime(), saved.getEndTime(),
                saved.getOverallFeeling(), suggestions);
    }
}
