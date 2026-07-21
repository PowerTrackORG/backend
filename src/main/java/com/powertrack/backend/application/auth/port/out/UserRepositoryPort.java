package com.powertrack.backend.application.auth.port.out;

import com.powertrack.backend.domain.user.User;

import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
