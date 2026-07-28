package com.powertrack.backend.domain.analytics;

/**
 * Compara la "serie de referencia" ({@link SessionAggregate#referenceSet()}) de las 2
 * sesiones COMPLETADAS más recientes de un mismo {@code routineExercise} y clasifica la
 * evolución (módulo Analytics, endpoint "por rutina"). Clase pura: sin dependencias de
 * Spring ni de JPA.
 * <p>
 * Nunca decide {@link ProgressStatus#INSUFFICIENT_DATA}: esa decisión depende de cuántas
 * sesiones históricas existen (menos de 2), algo que solo conoce el caller
 * ({@code GetRoutineAnalyticsService}), no esta clase.
 *
 * <h2>Reglas de clasificación (negocio ya validado)</h2>
 * <ul>
 *   <li><b>PROGRESSED</b>: peso subió (con reps y RPE iguales o mejores) O (peso igual Y
 *   reps subieron) O (peso y reps iguales Y RPE bajó).</li>
 *   <li><b>MAINTAINED</b>: peso, reps y RPE exactamente iguales.</li>
 *   <li><b>REGRESSED</b>: peso bajó, O reps bajaron a igual peso, O RPE subió a igual
 *   peso y reps.</li>
 * </ul>
 *
 * <h2>Gap consciente en la matriz (documentado, no silencioso)</h2>
 * De las 27 combinaciones posibles de (peso↑/=/↓) × (reps↑/=/↓) × (RPE↑/=/↓), 5 no caen
 * literalmente en ninguna de las 3 reglas de arriba. Las 5 comparten que el peso subió,
 * pero el RPE también subió y/o las reps bajaron:
 * <ul>
 *   <li>peso↑, reps=, RPE= (sin cambio en el desempeño relativo, no calificado por
 *   ninguna regla porque la Regla 1 exige RPE igual-o-mejor, no igual).</li>
 *   <li>peso↑, reps=, RPE↑</li>
 *   <li>peso↑, reps↓, RPE=</li>
 *   <li>peso↑, reps↓, RPE↑ (más carga pero con peor desempeño relativo — el ejemplo
 *   citado en el encargo de este módulo)</li>
 *   <li>peso↑, reps↓, RPE↓</li>
 * </ul>
 * Mismo criterio que el gap ya documentado y resuelto en {@code ProgressionRuleEngine}
 * (ver {@code Docs/decisiones-tecnicas.md}, entradas 2026-07-22 y 2026-07-23): se
 * devuelve {@link ProgressStatus#MAINTAINED} por defecto, en vez de inventar una 4ª
 * regla no solicitada. Señalado explícitamente para que producto decida si esto merece
 * una regla propia.
 */
public final class ProgressClassifier {

    private ProgressClassifier() {
    }

    public static ProgressStatus classify(SetSample previous, SetSample last) {
        int weightCmp = last.weightKg().compareTo(previous.weightKg());
        int repsCmp = Integer.compare(last.repsCompleted(), previous.repsCompleted());
        int rpeCmp = Integer.compare(last.rpe(), previous.rpe());

        boolean weightUp = weightCmp > 0;
        boolean weightSame = weightCmp == 0;
        boolean weightDown = weightCmp < 0;
        boolean repsUp = repsCmp > 0;
        boolean repsSame = repsCmp == 0;
        boolean repsDown = repsCmp < 0;
        boolean rpeUp = rpeCmp > 0;
        boolean rpeDown = rpeCmp < 0;

        if (weightSame && repsSame && rpeCmp == 0) {
            return ProgressStatus.MAINTAINED;
        }

        boolean progressed = (weightUp && !repsDown && !rpeUp)
                || (weightSame && repsUp)
                || (weightSame && repsSame && rpeDown);
        if (progressed) {
            return ProgressStatus.PROGRESSED;
        }

        boolean regressed = weightDown
                || (weightSame && repsDown)
                || (weightSame && repsSame && rpeUp);
        if (regressed) {
            return ProgressStatus.REGRESSED;
        }

        // Gap de la matriz (ver Javadoc de la clase): ningún patrón exacto aplica.
        return ProgressStatus.MAINTAINED;
    }
}
