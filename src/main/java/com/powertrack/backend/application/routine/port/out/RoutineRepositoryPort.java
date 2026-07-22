package com.powertrack.backend.application.routine.port.out;

import com.powertrack.backend.domain.routine.Routine;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineRepositoryPort {

    Routine save(Routine routine);

    Optional<Routine> findByIdAndUserId(UUID id, UUID userId);

    List<RoutineSummary> findSummariesByUserId(UUID userId);

    /**
     * @return {@code true} si existía y pertenecía a {@code userId} (y se eliminó),
     * {@code false} en caso contrario.
     */
    boolean deleteByIdAndUserId(UUID id, UUID userId);

    /**
     * Ownership check de solo lectura reutilizado por el módulo de Registro/Progreso
     * (workout) para validar, sin duplicar la lógica de pertenencia, que un
     * {@code routineDayId} exista y pertenezca a una rutina del usuario antes de iniciar
     * una sesión de entrenamiento sobre él.
     */
    boolean existsRoutineDayOwnedByUser(UUID routineDayId, UUID userId);

    /**
     * Targets configurados por el propio usuario para un {@code routineExercise},
     * reutilizado por el módulo de Registro/Progreso para evaluar el motor de
     * sugerencias (PRD §14) siempre contra lo que el usuario definió en su rutina, nunca
     * contra un estándar genérico. Devuelve vacío si el ejercicio no existe o pertenece a
     * la rutina de otro usuario (mismo criterio de ownership a nivel de query).
     */
    Optional<RoutineExerciseTargets> findRoutineExerciseTargets(UUID routineExerciseId, UUID userId);

    /**
     * Proyección liviana para el listado de rutinas: evita traer días/ejercicios
     * completos cuando la pantalla solo necesita nombre y cantidad de días (RNF-04).
     */
    record RoutineSummary(UUID id, String name, String description, Instant createdAt, int dayCount) {
    }

    record RoutineExerciseTargets(UUID routineExerciseId, int targetSets, int targetRepMin, int targetRepMax) {
    }
}
