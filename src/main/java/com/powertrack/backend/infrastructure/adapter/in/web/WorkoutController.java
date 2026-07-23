package com.powertrack.backend.infrastructure.adapter.in.web;

import com.powertrack.backend.application.workout.port.in.FinishWorkoutSessionUseCase;
import com.powertrack.backend.application.workout.port.in.GetPreviousLogUseCase;
import com.powertrack.backend.application.workout.port.in.StartWorkoutSessionUseCase;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.FinishWorkoutSessionRequest;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.FinishWorkoutSessionResponse;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.PreviousLogResponse;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.StartWorkoutSessionRequest;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.StartWorkoutSessionResponse;
import com.powertrack.backend.infrastructure.security.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Todos los endpoints requieren JWT válido (ya cubierto por defecto en
 * {@code SecurityConfig}). El userId se extrae siempre del SecurityContext, nunca del
 * body/query, siguiendo el mismo criterio que {@code RoutineController}.
 */
@RestController
@RequestMapping("/api/v1/workouts")
public class WorkoutController {

    private final StartWorkoutSessionUseCase startWorkoutSessionUseCase;
    private final GetPreviousLogUseCase getPreviousLogUseCase;
    private final FinishWorkoutSessionUseCase finishWorkoutSessionUseCase;

    public WorkoutController(StartWorkoutSessionUseCase startWorkoutSessionUseCase,
                              GetPreviousLogUseCase getPreviousLogUseCase,
                              FinishWorkoutSessionUseCase finishWorkoutSessionUseCase) {
        this.startWorkoutSessionUseCase = startWorkoutSessionUseCase;
        this.getPreviousLogUseCase = getPreviousLogUseCase;
        this.finishWorkoutSessionUseCase = finishWorkoutSessionUseCase;
    }

    @PostMapping("/session/start")
    public ResponseEntity<StartWorkoutSessionResponse> start(@Valid @RequestBody StartWorkoutSessionRequest request) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = startWorkoutSessionUseCase.start(request.toCommand(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(StartWorkoutSessionResponse.from(result));
    }

    /**
     * Decisión de diseño: se responde 204 No Content (en vez de 200 con un cuerpo
     * vacío/nulo) cuando el usuario no tiene histórico previo para el ejercicio. No es un
     * error (US-03 contempla explícitamente el caso "sin histórico"), y 204 deja claro al
     * cliente Android que no hay body que parsear sin necesidad de inspeccionar campos
     * nulos.
     */
    @GetMapping("/previous-log")
    public ResponseEntity<PreviousLogResponse> previousLog(@RequestParam UUID routineExerciseId) {
        UUID userId = CurrentUserResolver.currentUserId();
        return getPreviousLogUseCase.getPrevious(routineExerciseId, userId)
                .map(result -> ResponseEntity.ok(PreviousLogResponse.from(result)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/session/{sessionId}/finish")
    public ResponseEntity<FinishWorkoutSessionResponse> finish(@PathVariable UUID sessionId,
                                                                @Valid @RequestBody FinishWorkoutSessionRequest request) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = finishWorkoutSessionUseCase.finish(request.toCommand(sessionId, userId));
        return ResponseEntity.ok(FinishWorkoutSessionResponse.from(result));
    }
}
