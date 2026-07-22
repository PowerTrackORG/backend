package com.powertrack.backend.application.user.port.in;

import com.powertrack.backend.domain.user.SportGoal;

import java.util.UUID;

public interface UpdateSportGoalUseCase {

    /**
     * @param userId  usuario autenticado (extraído del JWT, nunca del body/query).
     * @param newGoal nuevo objetivo deportivo.
     * @throws com.powertrack.backend.application.user.exception.UserNotFoundException
     *         si el usuario ya no existe.
     */
    UserProfileResult updateSportGoal(UUID userId, SportGoal newGoal);
}
