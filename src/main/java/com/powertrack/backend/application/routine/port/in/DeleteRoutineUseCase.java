package com.powertrack.backend.application.routine.port.in;

import java.util.UUID;

public interface DeleteRoutineUseCase {

    /**
     * @throws com.powertrack.backend.application.routine.exception.RoutineNotFoundException
     *         si la rutina no existe o no pertenece a {@code userId}.
     */
    void delete(UUID routineId, UUID userId);
}
