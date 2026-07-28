package com.powertrack.backend.domain.analytics;

import com.powertrack.backend.domain.workout.OneRepMaxCalculator;

import java.math.BigDecimal;
import java.util.List;

/**
 * Agrega el desempeño de todas las series de un ejercicio dentro de una sola sesión
 * (módulo Analytics: endpoints "por ejercicio", "por músculo" y "por rutina"). Clase
 * pura: sin dependencias de Spring ni de JPA.
 * <p>
 * <b>Volumen</b> (tonelaje): suma de {@code weightKg * repsCompleted} de TODAS las
 * series recibidas, sin filtrar por rango de repeticiones — a diferencia del 1RM
 * estimado, que sí exige que la serie califique (ver {@link OneRepMaxCalculator}).
 * <p>
 * <b>Serie de referencia</b> ({@code referenceSet}): la de mayor 1RM estimado entre las
 * series que califican (1-12 repeticiones). Si ninguna serie califica, se usa la de
 * mayor peso y {@code estimated1RM} queda en {@code null}.
 * <p>
 * <b>Desempate:</b> si dos o más series empatan en el criterio de selección (mismo e1RM
 * estimado, o mismo peso máximo cuando ninguna califica), se elige la primera en el
 * orden en que llegan en {@code sets}. El caller es responsable de pasarlas ya
 * ordenadas por {@code setNumber} si el orden del desempate importa para su caso de uso.
 */
public final class SessionPerformanceAggregator {

    private SessionPerformanceAggregator() {
    }

    public static SessionAggregate aggregate(List<SetSample> sets) {
        if (sets == null || sets.isEmpty()) {
            throw new IllegalArgumentException("sets no puede estar vacío");
        }

        BigDecimal volumeKg = BigDecimal.ZERO;
        BigDecimal maxWeightKg = null;
        int totalReps = 0;
        int totalRpe = 0;

        SetSample maxWeightSet = null;
        SetSample bestQualifyingSet = null;
        BigDecimal bestEstimated1RM = null;

        for (SetSample set : sets) {
            volumeKg = volumeKg.add(set.weightKg().multiply(BigDecimal.valueOf(set.repsCompleted())));
            totalReps += set.repsCompleted();
            totalRpe += set.rpe();

            if (maxWeightKg == null || set.weightKg().compareTo(maxWeightKg) > 0) {
                maxWeightKg = set.weightKg();
            }
            if (maxWeightSet == null || set.weightKg().compareTo(maxWeightSet.weightKg()) > 0) {
                maxWeightSet = set;
            }
            if (OneRepMaxCalculator.qualifies(set.repsCompleted())) {
                BigDecimal estimated = OneRepMaxCalculator.estimate(set.weightKg(), set.repsCompleted());
                if (bestEstimated1RM == null || estimated.compareTo(bestEstimated1RM) > 0) {
                    bestEstimated1RM = estimated;
                    bestQualifyingSet = set;
                }
            }
        }

        double avgReps = (double) totalReps / sets.size();
        double avgRpe = (double) totalRpe / sets.size();

        SetSample referenceSet = bestQualifyingSet != null ? bestQualifyingSet : maxWeightSet;
        BigDecimal estimated1RM = bestQualifyingSet != null ? bestEstimated1RM : null;

        return new SessionAggregate(maxWeightKg, volumeKg, avgReps, avgRpe, referenceSet, estimated1RM);
    }
}
