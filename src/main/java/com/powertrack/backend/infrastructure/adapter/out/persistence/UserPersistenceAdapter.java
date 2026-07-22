package com.powertrack.backend.infrastructure.adapter.out.persistence;

import com.powertrack.backend.application.auth.port.out.UserRepositoryPort;
import com.powertrack.backend.domain.user.User;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpaRepository;

    public UserPersistenceAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                user.getId(), user.getEmail(), user.getPasswordHash(),
                user.getFullName(), user.getSportGoal(), user.getCreatedAt());
        UserJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    private User toDomain(UserJpaEntity entity) {
        return User.rehydrate(
                entity.getId(), entity.getEmail(), entity.getPasswordHash(),
                entity.getFullName(), entity.getSportGoal(), entity.getCreatedAt());
    }
}
