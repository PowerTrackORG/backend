package com.powertrack.backend.application.analytics.port.in;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Distribución de tonelaje (volumen) por grupo muscular ({@code Exercise.targetMuscle})
 * a través de todas las sesiones COMPLETADAS del usuario autenticado en la ventana dada
 * (módulo Analytics). Lista ordenada por volumen descendente.
 */
public interface GetMuscleGroupVolumeUseCase {

    List<MuscleGroupVolumeResult> getMuscleGroupVolume(UUID userId, Instant from, Instant to);

    record MuscleGroupVolumeResult(String targetMuscle, BigDecimal totalVolumeKg) {
    }
}
