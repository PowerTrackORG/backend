package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.exception.ExerciseNotFoundException;
import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase;
import com.powertrack.backend.application.routine.port.in.RoutineDetailResult;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.domain.routine.Exercise;
import com.powertrack.backend.domain.routine.Routine;
import com.powertrack.backend.domain.routine.RoutineDay;
import com.powertrack.backend.domain.routine.RoutineExercise;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CreateRoutineService implements CreateRoutineUseCase {

    private final RoutineRepositoryPort routineRepository;
    private final ExerciseRepositoryPort exerciseRepository;

    public CreateRoutineService(RoutineRepositoryPort routineRepository, ExerciseRepositoryPort exerciseRepository) {
        this.routineRepository = routineRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public RoutineDetailResult create(CreateRoutineCommand command) {
        // Idempotencia de sync offline: si este routineId (generado por el cliente) ya
        // fue creado por este mismo usuario, es un reintento — se devuelve tal cual sin
        // volver a insertar ni pisar cambios. Ver Docs/decisiones-tecnicas.md, 2026-07-23.
        Optional<Routine> existing = routineRepository.findByIdAndUserId(command.routineId(), command.userId());
        if (existing.isPresent()) {
            List<UUID> existingExerciseIds = existing.get().getDays().stream()
                    .flatMap(day -> day.getExercises().stream())
                    .map(RoutineExercise::getExerciseId)
                    .distinct()
                    .toList();
            return RoutineMapper.toDetailResult(existing.get(), exerciseRepository.findAllByIds(existingExerciseIds));
        }
        // El routineId existe pero pertenece a otro usuario: colisión real (o bug de
        // cliente), no un reintento legítimo — se rechaza en vez de sobreescribir datos
        // ajenos.
        if (routineRepository.existsById(command.routineId())) {
            throw new IllegalArgumentException("routineId ya está en uso");
        }

        List<UUID> exerciseIds = command.days().stream()
                .flatMap(day -> day.exercises().stream())
                .map(RoutineExerciseCommand::exerciseId)
                .distinct()
                .toList();

        Map<UUID, Exercise> exercisesById = exerciseRepository.findAllByIds(exerciseIds);
        for (UUID exerciseId : exerciseIds) {
            if (!exercisesById.containsKey(exerciseId)) {
                throw new ExerciseNotFoundException(exerciseId);
            }
        }

        List<RoutineDay> days = command.days().stream()
                .map(this::toDomainDay)
                .toList();

        Routine routine = Routine.create(command.routineId(), command.userId(), command.name(), command.description(), days);
        Routine saved = routineRepository.save(routine);

        return RoutineMapper.toDetailResult(saved, exercisesById);
    }

    private RoutineDay toDomainDay(RoutineDayCommand dayCommand) {
        List<RoutineExercise> exercises = dayCommand.exercises().stream()
                .map(this::toDomainExercise)
                .toList();
        return RoutineDay.create(dayCommand.dayName(), dayCommand.orderIndex(), exercises);
    }

    private RoutineExercise toDomainExercise(RoutineExerciseCommand exerciseCommand) {
        return RoutineExercise.create(exerciseCommand.exerciseId(), exerciseCommand.orderIndex(),
                exerciseCommand.targetSets(), exerciseCommand.targetRepMin(), exerciseCommand.targetRepMax(),
                exerciseCommand.restSeconds(), exerciseCommand.notes());
    }
}
