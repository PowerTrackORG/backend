package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.port.in.ExerciseResult;
import com.powertrack.backend.application.routine.port.in.ListExercisesUseCase;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListExercisesService implements ListExercisesUseCase {

    private final ExerciseRepositoryPort exerciseRepository;

    public ListExercisesService(ExerciseRepositoryPort exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public List<ExerciseResult> list(UUID userId) {
        return exerciseRepository.findPredefinedAndByUser(userId).stream()
                .map(exercise -> new ExerciseResult(exercise.getId(), exercise.getName(), exercise.getTargetMuscle(),
                        exercise.getCategory(), exercise.isPredefined()))
                .toList();
    }
}
