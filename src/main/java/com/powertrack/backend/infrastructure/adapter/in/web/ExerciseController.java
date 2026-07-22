package com.powertrack.backend.infrastructure.adapter.in.web;

import com.powertrack.backend.application.routine.port.in.CreateExerciseUseCase;
import com.powertrack.backend.application.routine.port.in.CreateExerciseUseCase.CreateExerciseCommand;
import com.powertrack.backend.application.routine.port.in.ListExercisesUseCase;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.CreateExerciseRequest;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.ExerciseResponse;
import com.powertrack.backend.infrastructure.security.CurrentUserResolver;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exercises")
public class ExerciseController {

    private final ListExercisesUseCase listExercisesUseCase;
    private final CreateExerciseUseCase createExerciseUseCase;

    public ExerciseController(ListExercisesUseCase listExercisesUseCase, CreateExerciseUseCase createExerciseUseCase) {
        this.listExercisesUseCase = listExercisesUseCase;
        this.createExerciseUseCase = createExerciseUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ExerciseResponse>> list() {
        UUID userId = CurrentUserResolver.currentUserId();
        List<ExerciseResponse> results = listExercisesUseCase.list(userId).stream()
                .map(ExerciseResponse::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ExerciseResponse> create(@Valid @RequestBody CreateExerciseRequest request) {
        UUID userId = CurrentUserResolver.currentUserId();
        var result = createExerciseUseCase.create(
                new CreateExerciseCommand(userId, request.name(), request.targetMuscle(), request.category()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ExerciseResponse.from(result));
    }
}
