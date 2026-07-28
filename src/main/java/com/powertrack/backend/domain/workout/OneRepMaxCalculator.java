package com.powertrack.backend.domain.workout;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Estimador de 1RM (una repetición máxima) usando la fórmula de Epley:
 * {@code peso * (1 + reps/30)}. Módulo Analytics (histórico de progreso por ejercicio).
 * Clase pura: sin dependencias de Spring ni de JPA.
 * <p>
 * La fórmula solo se considera fiable dentro de un rango acotado de repeticiones
 * (1-12 inclusive): fuera de ese rango el margen de error de cualquier fórmula de
 * estimación de 1RM crece demasiado para ser una referencia útil de progreso. Series
 * fuera de rango simplemente no participan del cálculo de 1RM (aunque sí participan del
 * cálculo de volumen/tonelaje, que no tiene esta restricción).
 */
public final class OneRepMaxCalculator {

    private static final int MIN_QUALIFYING_REPS = 1;
    private static final int MAX_QUALIFYING_REPS = 12;
    private static final int INTERMEDIATE_SCALE = 10;

    private OneRepMaxCalculator() {
    }

    public static boolean qualifies(int repsCompleted) {
        return repsCompleted >= MIN_QUALIFYING_REPS && repsCompleted <= MAX_QUALIFYING_REPS;
    }

    /**
     * @throws IllegalArgumentException si {@code repsCompleted} no califica (ver
     *                                   {@link #qualifies(int)}).
     */
    public static BigDecimal estimate(BigDecimal weightKg, int repsCompleted) {
        if (!qualifies(repsCompleted)) {
            throw new IllegalArgumentException(
                    "repsCompleted debe estar entre " + MIN_QUALIFYING_REPS + " y " + MAX_QUALIFYING_REPS
                            + " para estimar 1RM (Epley), recibido: " + repsCompleted);
        }
        BigDecimal factor = BigDecimal.ONE.add(
                BigDecimal.valueOf(repsCompleted).divide(BigDecimal.valueOf(30), INTERMEDIATE_SCALE, RoundingMode.HALF_UP));
        return weightKg.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
