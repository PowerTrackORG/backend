package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.analytics.port.in.GetMuscleGroupVolumeUseCase.MuscleGroupVolumeResult;

import java.math.BigDecimal;

public record MuscleGroupVolumeResponse(String targetMuscle, BigDecimal totalVolumeKg) {

    public static MuscleGroupVolumeResponse from(MuscleGroupVolumeResult result) {
        return new MuscleGroupVolumeResponse(result.targetMuscle(), result.totalVolumeKg());
    }
}
