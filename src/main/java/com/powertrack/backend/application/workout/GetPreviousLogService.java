package com.powertrack.backend.application.workout;

import com.powertrack.backend.application.workout.port.in.GetPreviousLogUseCase;
import com.powertrack.backend.application.workout.port.out.WorkoutLogRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GetPreviousLogService implements GetPreviousLogUseCase {

    private final WorkoutLogRepositoryPort workoutLogRepository;

    public GetPreviousLogService(WorkoutLogRepositoryPort workoutLogRepository) {
        this.workoutLogRepository = workoutLogRepository;
    }

    @Override
    public Optional<PreviousLogResult> getPrevious(UUID routineExerciseId, UUID userId) {
        return workoutLogRepository.findLatestByRoutineExerciseIdAndUserId(routineExerciseId, userId)
                .map(previousLog -> toResult(routineExerciseId, previousLog));
    }

    private PreviousLogResult toResult(UUID routineExerciseId, WorkoutLogRepositoryPort.PreviousLog previousLog) {
        List<LogSetResult> sets = previousLog.sets().stream()
                .map(s -> new LogSetResult(s.setNumber(), s.weightKg(), s.repsCompleted(), s.rpe()))
                .toList();
        return new PreviousLogResult(previousLog.workoutLogId(), routineExerciseId, previousLog.performedAt(),
                previousLog.notes(), sets);
    }
}
