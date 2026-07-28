package com.powertrack.backend.domain.analytics;

import java.math.BigDecimal;

/**
 * Modelo de dominio puro (módulo Analytics). No conoce JPA, Spring ni ningún framework.
 * Representa el desempeño de una serie (peso/reps/RPE) de forma desacoplada de
 * {@link com.powertrack.backend.domain.workout.LogSet}: Analytics no necesita el
 * {@code id} ni el {@code setNumber} de la serie, solo los tres valores que alimentan el
 * cálculo de 1RM/volumen/comparación de progreso.
 */
public record SetSample(BigDecimal weightKg, int repsCompleted, int rpe) {
}
