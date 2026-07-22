package com.powertrack.backend.domain.workout;

/**
 * Sensación general de la SESIÓN completa (RF-04, US-06), no del ejercicio individual.
 * Se captura una sola vez al finalizar la sesión ({@link WorkoutSession#finish}) y se
 * reutiliza para evaluar cada ejercicio en {@link ProgressionRuleEngine}, tal como la
 * describen las Reglas 1, 3, 4 y 5 de la sección 14 del PRD ("sensación general de la
 * sesión").
 */
public enum OverallFeeling {
    MALA,
    REGULAR,
    BUENA,
    EXCELENTE
}
