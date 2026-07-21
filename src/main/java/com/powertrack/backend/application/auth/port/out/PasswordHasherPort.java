package com.powertrack.backend.application.auth.port.out;

/**
 * Aísla al dominio/aplicación del algoritmo de hashing concreto (BCrypt vive en infrastructure).
 */
public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
