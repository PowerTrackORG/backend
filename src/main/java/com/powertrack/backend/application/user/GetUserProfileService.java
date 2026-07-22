package com.powertrack.backend.application.user;

import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.application.user.exception.UserNotFoundException;
import com.powertrack.backend.application.user.port.in.GetUserProfileUseCase;
import com.powertrack.backend.application.user.port.in.UserProfileResult;
import com.powertrack.backend.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetUserProfileService implements GetUserProfileUseCase {

    private final UserRepositoryPort userRepository;

    public GetUserProfileService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfileResult getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserMapper.toResult(user);
    }
}
