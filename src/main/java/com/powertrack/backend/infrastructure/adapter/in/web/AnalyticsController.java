package com.powertrack.backend.infrastructure.adapter.in.web;

import com.powertrack.backend.application.analytics.port.in.GetExerciseAnalyticsUseCase;
import com.powertrack.backend.application.analytics.port.in.GetMuscleGroupVolumeUseCase;
import com.powertrack.backend.application.analytics.port.in.GetRoutineAnalyticsUseCase;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.ExerciseAnalyticsResponse;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.MuscleGroupVolumeResponse;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.RoutineAnalyticsResponse;
import com.powertrack.backend.infrastructure.security.CurrentUserResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Todos los endpoints requieren JWT válido (ya cubierto por defecto en
 * {@code SecurityConfig}). El userId se extrae siempre del SecurityContext, nunca del
 * body/query, mismo criterio que {@code RoutineController}/{@code WorkoutController}.
 * Cada caso de uso aplica su propia ventana por defecto cuando {@code from}/{@code to} no
 * se envían (ver Javadoc de cada servicio de aplicación).
 */
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final GetExerciseAnalyticsUseCase getExerciseAnalyticsUseCase;
    private final GetMuscleGroupVolumeUseCase getMuscleGroupVolumeUseCase;
    private final GetRoutineAnalyticsUseCase getRoutineAnalyticsUseCase;

    public AnalyticsController(GetExerciseAnalyticsUseCase getExerciseAnalyticsUseCase,
                                GetMuscleGroupVolumeUseCase getMuscleGroupVolumeUseCase,
                                GetRoutineAnalyticsUseCase getRoutineAnalyticsUseCase) {
        this.getExerciseAnalyticsUseCase = getExerciseAnalyticsUseCase;
        this.getMuscleGroupVolumeUseCase = getMuscleGroupVolumeUseCase;
        this.getRoutineAnalyticsUseCase = getRoutineAnalyticsUseCase;
    }

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<ExerciseAnalyticsResponse> exerciseAnalytics(@PathVariable UUID exerciseId,
                                                                        @RequestParam(required = false) Instant from,
                                                                        @RequestParam(required = false) Instant to) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = getExerciseAnalyticsUseCase.getExerciseAnalytics(exerciseId, userId, from, to);
        return ResponseEntity.ok(ExerciseAnalyticsResponse.from(result));
    }

    @GetMapping("/muscle-group")
    public ResponseEntity<List<MuscleGroupVolumeResponse>> muscleGroupVolume(@RequestParam(required = false) Instant from,
                                                                              @RequestParam(required = false) Instant to) {
        UUID userId = CurrentUserResolver.currentUserId();
        List<MuscleGroupVolumeResponse> results = getMuscleGroupVolumeUseCase.getMuscleGroupVolume(userId, from, to).stream()
                .map(MuscleGroupVolumeResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/routine/{routineId}")
    public ResponseEntity<RoutineAnalyticsResponse> routineAnalytics(@PathVariable UUID routineId,
                                                                      @RequestParam(required = false) Instant from,
                                                                      @RequestParam(required = false) Instant to) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = getRoutineAnalyticsUseCase.getRoutineAnalytics(routineId, userId, from, to);
        return ResponseEntity.ok(RoutineAnalyticsResponse.from(result));
    }
}
