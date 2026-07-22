package com.powertrack.backend.domain.workout;

/**
 * Resultado del motor determinista de sugerencias (PRD §14). Cada valor lleva el texto
 * de sugerencia exacto propuesto en el PRD para no reinterpretar el copy de negocio.
 */
public enum Recommendation {

    INCREASE_WEIGHT("Aumenta entre un 2.5% y 5% el peso para la próxima sesión (+1.25kg - +2.5kg por lado)."),
    INCREASE_REPS("Conserva el peso actual. Intenta agregar 1 repetición extra por serie la próxima sesión."),
    MAINTAIN("Buena intensidad. Consolida el mismo peso y repeticiones antes de subir la carga."),
    DECREASE_WEIGHT("Carga muy alta para la capacidad actual. Reduce el peso un 5% - 10% para garantizar técnica correcta."),
    DELOAD("Acumulación de fatiga detectada. Se sugiere una semana de descarga (Deload) reduciendo el volumen al 50% y la carga al 70%.");

    private final String defaultMessage;

    Recommendation(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
