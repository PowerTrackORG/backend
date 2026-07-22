package com.powertrack.backend.application.routine.port.in;

import java.util.UUID;

public interface GetRoutineDetailUseCase {

    /**
     * @param routineId rutina solicitada.
     * @param userId    usuario autenticado (dueño esperado).
     * @throws com.powertrack.backend.application.routine.exception.RoutineNotFoundException
     *         si la rutina no existe o no pertenece a {@code userId}.
     */
    RoutineDetailResult getDetail(UUID routineId, UUID userId);
}
