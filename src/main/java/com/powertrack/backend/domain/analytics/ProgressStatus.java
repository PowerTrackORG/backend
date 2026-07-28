package com.powertrack.backend.domain.analytics;

/**
 * Clasificación de la evolución de un {@code routineExercise} entre sus 2 sesiones
 * COMPLETADAS más recientes (módulo Analytics, endpoint "por rutina"). Ver
 * {@link ProgressClassifier}.
 */
public enum ProgressStatus {
    PROGRESSED,
    MAINTAINED,
    REGRESSED,
    INSUFFICIENT_DATA
}
