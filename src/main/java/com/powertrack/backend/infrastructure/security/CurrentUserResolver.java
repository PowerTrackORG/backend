package com.powertrack.backend.infrastructure.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/**
 * Helper de adaptador de entrada: extrae el {@code userId} ya autenticado por el
 * {@link JwtAuthenticationFilter} del {@code SecurityContext}. Nunca se debe confiar en
 * un userId que venga del body/query si ya existe en el token.
 */
public final class CurrentUserResolver {

    private CurrentUserResolver() {
    }

    public static UUID currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString((String) principal);
    }
}
