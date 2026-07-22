package com.powertrack.backend.application.user;

import com.powertrack.backend.application.user.port.in.UserProfileResult;
import com.powertrack.backend.domain.user.User;

final class UserMapper {

    private UserMapper() {
    }

    static UserProfileResult toResult(User user) {
        return new UserProfileResult(user.getId(), user.getEmail(), user.getFullName(), user.getSportGoal(), user.getCreatedAt());
    }
}
