package com.powertrack.backend.infrastructure.adapter.in.web;

import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase;
import com.powertrack.backend.application.routine.port.in.DeleteRoutineUseCase;
import com.powertrack.backend.application.routine.port.in.GetRoutineDetailUseCase;
import com.powertrack.backend.application.routine.port.in.ListRoutinesUseCase;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.CreateRoutineRequest;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.RoutineDetailResponse;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.RoutineSummaryResponse;
import com.powertrack.backend.infrastructure.security.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Todos los endpoints requieren JWT válido (ya cubierto por defecto en {@code SecurityConfig},
 * que solo permite libre acceso a {@code /api/v1/auth/**}). El userId se extrae siempre del
 * SecurityContext, nunca del body/query.
 */
@RestController
@RequestMapping("/api/v1/routines")
public class RoutineController {

    private final CreateRoutineUseCase createRoutineUseCase;
    private final ListRoutinesUseCase listRoutinesUseCase;
    private final GetRoutineDetailUseCase getRoutineDetailUseCase;
    private final DeleteRoutineUseCase deleteRoutineUseCase;

    public RoutineController(CreateRoutineUseCase createRoutineUseCase, ListRoutinesUseCase listRoutinesUseCase,
                              GetRoutineDetailUseCase getRoutineDetailUseCase, DeleteRoutineUseCase deleteRoutineUseCase) {
        this.createRoutineUseCase = createRoutineUseCase;
        this.listRoutinesUseCase = listRoutinesUseCase;
        this.getRoutineDetailUseCase = getRoutineDetailUseCase;
        this.deleteRoutineUseCase = deleteRoutineUseCase;
    }

    @PostMapping
    public ResponseEntity<RoutineDetailResponse> create(@Valid @RequestBody CreateRoutineRequest request) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = createRoutineUseCase.create(request.toCommand(userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(RoutineDetailResponse.from(result));
    }

    @GetMapping
    public ResponseEntity<List<RoutineSummaryResponse>> list() {
        UUID userId = CurrentUserResolver.currentUserId();
        List<RoutineSummaryResponse> results = listRoutinesUseCase.list(userId).stream()
                .map(RoutineSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutineDetailResponse> getDetail(@PathVariable UUID id) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = getRoutineDetailUseCase.getDetail(id, userId);
        return ResponseEntity.ok(RoutineDetailResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        UUID userId = CurrentUserResolver.currentUserId();
        deleteRoutineUseCase.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
