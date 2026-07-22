package com.powertrack.backend.application.user;

import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.application.user.exception.UserNotFoundException;
import com.powertrack.backend.application.user.port.in.UpdateSportGoalUseCase;
import com.powertrack.backend.application.user.port.in.UserProfileResult;
import com.powertrack.backend.domain.user.SportGoal;
import com.powertrack.backend.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateSportGoalService implements UpdateSportGoalUseCase {

    private final UserRepositoryPort userRepository;

    public UpdateSportGoalService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileResult updateSportGoal(UUID userId, SportGoal newGoal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        User updated = user.withSportGoal(newGoal);
        User saved = userRepository.save(updated);
        return UserMapper.toResult(saved);
    }
}
