package com.powertrack.backend.infrastructure.adapter.in.web;

import com.powertrack.backend.application.user.port.in.GetUserProfileUseCase;
import com.powertrack.backend.application.user.port.in.UpdateSportGoalUseCase;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.UpdateSportGoalRequest;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.UserProfileResponse;
import com.powertrack.backend.infrastructure.security.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Requiere JWT válido (ya cubierto por defecto en {@code SecurityConfig}). El userId se
 * extrae siempre del SecurityContext, nunca del body/query.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateSportGoalUseCase updateSportGoalUseCase;

    public UserController(GetUserProfileUseCase getUserProfileUseCase, UpdateSportGoalUseCase updateSportGoalUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateSportGoalUseCase = updateSportGoalUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile() {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = getUserProfileUseCase.getProfile(userId);
        return ResponseEntity.ok(UserProfileResponse.from(result));
    }

    @PutMapping("/me/goal")
    public ResponseEntity<UserProfileResponse> updateMySportGoal(@Valid @RequestBody UpdateSportGoalRequest request) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = updateSportGoalUseCase.updateSportGoal(userId, request.sportGoal());
        return ResponseEntity.ok(UserProfileResponse.from(result));
    }
}
