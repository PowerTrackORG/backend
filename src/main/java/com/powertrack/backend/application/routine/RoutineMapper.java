package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.port.in.RoutineDetailResult;
import com.powertrack.backend.application.routine.port.in.RoutineDetailResult.RoutineDayResult;
import com.powertrack.backend.application.routine.port.in.RoutineDetailResult.RoutineExerciseResult;
import com.powertrack.backend.domain.routine.Exercise;
import com.powertrack.backend.domain.routine.Routine;
import com.powertrack.backend.domain.routine.RoutineDay;
import com.powertrack.backend.domain.routine.RoutineExercise;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mapper interno (no es un puerto): construye el {@link RoutineDetailResult} a partir del
 * agregado de dominio y del catálogo de ejercicios ya resuelto, compartido entre
 * {@link CreateRoutineService} y {@link GetRoutineDetailService} para no duplicar el mapeo.
 */
final class RoutineMapper {

    private RoutineMapper() {
    }

    static RoutineDetailResult toDetailResult(Routine routine, Map<UUID, Exercise> exercisesById) {
        List<RoutineDayResult> days = routine.getDays().stream()
                .map(day -> toDayResult(day, exercisesById))
                .toList();
        return new RoutineDetailResult(routine.getId(), routine.getUserId(), routine.getName(),
                routine.getDescription(), routine.getCreatedAt(), days);
    }

    private static RoutineDayResult toDayResult(RoutineDay day, Map<UUID, Exercise> exercisesById) {
        List<RoutineExerciseResult> exercises = day.getExercises().stream()
                .map(exercise -> toExerciseResult(exercise, exercisesById))
                .toList();
        return new RoutineDayResult(day.getId(), day.getDayName(), day.getOrderIndex(), exercises);
    }

    private static RoutineExerciseResult toExerciseResult(RoutineExercise exercise, Map<UUID, Exercise> exercisesById) {
        Exercise catalogExercise = exercisesById.get(exercise.getExerciseId());
        String exerciseName = catalogExercise != null ? catalogExercise.getName() : null;
        return new RoutineExerciseResult(exercise.getId(), exercise.getExerciseId(), exerciseName,
                exercise.getOrderIndex(), exercise.getTargetSets(), exercise.getTargetRepMin(),
                exercise.getTargetRepMax(), exercise.getRestSeconds(), exercise.getNotes());
    }
}
