package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.exception.RoutineNotFoundException;
import com.powertrack.backend.application.routine.port.in.GetRoutineDetailUseCase;
import com.powertrack.backend.application.routine.port.in.RoutineDetailResult;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.domain.routine.Exercise;
import com.powertrack.backend.domain.routine.Routine;
import com.powertrack.backend.domain.routine.RoutineExercise;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class GetRoutineDetailService implements GetRoutineDetailUseCase {

    private final RoutineRepositoryPort routineRepository;
    private final ExerciseRepositoryPort exerciseRepository;

    public GetRoutineDetailService(RoutineRepositoryPort routineRepository, ExerciseRepositoryPort exerciseRepository) {
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public RoutineDetailResult getDetail(UUID routineId, UUID userId) {
        // findByIdAndUserId filtra por dueño a nivel de query: si la rutina existe pero
        // es de otro usuario, no se encuentra y se trata igual que "no existe" (404).
        Routine routine = routineRepository.findByIdAndUserId(routineId, userId)
                .orElseThrow(() -> new RoutineNotFoundException(routineId));

        List<UUID> exerciseIds = routine.getDays().stream()
                .flatMap(day -> day.getExercises().stream())
                .map(RoutineExercise::getExerciseId)
                .distinct()
                .toList();

        Map<UUID, Exercise> exercisesById = exerciseRepository.findAllByIds(exerciseIds);
        return RoutineMapper.toDetailResult(routine, exercisesById);
    }
}
