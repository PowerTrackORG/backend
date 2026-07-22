package com.powertrack.backend.application.user.port.in;

import java.util.UUID;

public interface GetUserProfileUseCase {

    /**
     * @param userId usuario autenticado (extraído del JWT, nunca del body/query).
     * @throws com.powertrack.backend.application.user.exception.UserNotFoundException
     *         si el usuario ya no existe.
     */
    UserProfileResult getProfile(UUID userId);
}
