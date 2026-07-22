package com.powertrack.backend.domain.user;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Modelo de dominio puro. No conoce JPA, Spring ni ningún framework:
 * los adaptadores de persistencia mapean esto hacia/desde su propia entidad.
 */
public final class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final SportGoal sportGoal;
    private final Instant createdAt;

    private User(UUID id, String email, String passwordHash, String fullName, SportGoal sportGoal, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.sportGoal = sportGoal;
        this.createdAt = createdAt;
    }

    public static User register(String email, String passwordHash, String fullName, SportGoal sportGoal) {
        Objects.requireNonNull(email, "email es obligatorio");
        Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
        Objects.requireNonNull(fullName, "fullName es obligatorio");
        Objects.requireNonNull(sportGoal, "sportGoal es obligatorio");
        return new User(UUID.randomUUID(), email, passwordHash, fullName, sportGoal, Instant.now());
    }

    public static User rehydrate(UUID id, String email, String passwordHash, String fullName, SportGoal sportGoal, Instant createdAt) {
        return new User(id, email, passwordHash, fullName, sportGoal, createdAt);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public SportGoal getSportGoal() {
        return sportGoal;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Devuelve una nueva instancia inmutable con el {@code sportGoal} actualizado,
     * preservando el resto de atributos (RF de edición de objetivo deportivo).
     */
    public User withSportGoal(SportGoal newSportGoal) {
        Objects.requireNonNull(newSportGoal, "sportGoal es obligatorio");
        return new User(id, email, passwordHash, fullName, newSportGoal, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
