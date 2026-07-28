package com.powertrack.backend.domain.analytics;

import java.math.BigDecimal;

/**
 * Modelo de dominio puro (módulo Analytics). Resultado de agregar todas las series de un
 * ejercicio dentro de una sola sesión, calculado por {@link SessionPerformanceAggregator}.
 *
 * @param maxWeightKg  peso máximo levantado entre todas las series de la sesión.
 * @param volumeKg     tonelaje total: suma de {@code weightKg * repsCompleted} de TODAS
 *                      las series (sin filtrar por rango de repeticiones).
 * @param avgReps      promedio de repeticiones logradas entre todas las series.
 * @param avgRpe       promedio de RPE entre todas las series.
 * @param referenceSet la serie de mayor 1RM estimado entre las que califican (ver
 *                      {@link com.powertrack.backend.domain.workout.OneRepMaxCalculator}),
 *                      o la de mayor peso si ninguna califica.
 * @param estimated1RM 1RM estimado (Epley) del {@code referenceSet}; {@code null} si
 *                      ninguna serie de la sesión calificó para el cálculo.
 */
public record SessionAggregate(BigDecimal maxWeightKg, BigDecimal volumeKg, double avgReps, double avgRpe,
                                SetSample referenceSet, BigDecimal estimated1RM) {
}
