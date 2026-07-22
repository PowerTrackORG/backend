package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.port.in.CreateExerciseUseCase;
import com.powertrack.backend.application.routine.port.in.ExerciseResult;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.domain.routine.Exercise;
import org.springframework.stereotype.Service;

@Service
public class CreateExerciseService implements CreateExerciseUseCase {

    private final ExerciseRepositoryPort exerciseRepository;

    public CreateExerciseService(ExerciseRepositoryPort exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public ExerciseResult create(CreateExerciseCommand command) {
        Exercise exercise = Exercise.create(command.name(), command.targetMuscle(), command.category(), command.userId());
        Exercise saved = exerciseRepository.save(exercise);
        return new ExerciseResult(saved.getId(), saved.getName(), saved.getTargetMuscle(), saved.getCategory(), saved.isPredefined());
    }
}
