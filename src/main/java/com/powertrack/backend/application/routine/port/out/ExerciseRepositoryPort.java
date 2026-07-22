package com.powertrack.backend.application.routine.port.out;

import com.powertrack.backend.domain.routine.Exercise;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ExerciseRepositoryPort {

    Exercise save(Exercise exercise);

    /**
     * Catálogo visible para el usuario: ejercicios predefinidos (createdByUserId nulo)
     * más los ejercicios personalizados creados por el propio usuario.
     */
    List<Exercise> findPredefinedAndByUser(UUID userId);

    /**
     * Lookup en batch por ids, para evitar N+1 al enriquecer el detalle de una rutina
     * con los nombres de los ejercicios referenciados.
     */
    Map<UUID, Exercise> findAllByIds(Collection<UUID> ids);

    Optional<Exercise> findById(UUID id);
}
