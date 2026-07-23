package com.powertrack.backend.application.auth;

import com.powertrack.backend.application.auth.exception.InvalidRefreshTokenException;
import com.powertrack.backend.application.auth.port.in.AuthResult;
import com.powertrack.backend.application.auth.port.in.RefreshAccessTokenUseCase;
import com.powertrack.backend.application.auth.port.out.TokenProviderPort;
import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RefreshAccessTokenService implements RefreshAccessTokenUseCase {

    private final UserRepositoryPort userRepository;
    private final TokenProviderPort tokenProvider;

    public RefreshAccessTokenService(UserRepositoryPort userRepository, TokenProviderPort tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthResult refresh(String refreshToken) {
        UUID userId = tokenProvider.validateRefreshTokenAndGetUserId(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        // Si el token era válido pero el usuario ya no existe (caso extremo, no hay
        // borrado de cuentas en el MVP), se trata igual que un token inválido: mismo
        // criterio anti-enumeración que el resto del módulo de Auth.
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);

        String newAccessToken = tokenProvider.generateAccessToken(user);
        // Sin rotación: no hay infraestructura de revocación de refresh tokens en el
        // MVP, así que devolver uno nuevo no aportaría seguridad real, solo complejidad.
        return new AuthResult(user.getId(), user.getEmail(), newAccessToken, refreshToken);
    }
}
