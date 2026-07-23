package com.powertrack.backend.infrastructure.adapter.in.web;

import com.powertrack.backend.application.auth.exception.EmailAlreadyRegisteredException;
import com.powertrack.backend.application.auth.exception.InvalidCredentialsException;
import com.powertrack.backend.application.auth.exception.InvalidRefreshTokenException;
import com.powertrack.backend.application.routine.exception.ExerciseNotFoundException;
import com.powertrack.backend.application.routine.exception.RoutineNotFoundException;
import com.powertrack.backend.application.workout.exception.RoutineDayNotFoundException;
import com.powertrack.backend.application.workout.exception.RoutineExerciseNotFoundException;
import com.powertrack.backend.application.workout.exception.WorkoutSessionAlreadyFinishedException;
import com.powertrack.backend.application.workout.exception.WorkoutSessionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return errorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(RoutineNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoutineNotFound(RoutineNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ExerciseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleExerciseNotFound(ExerciseNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WorkoutSessionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWorkoutSessionNotFound(WorkoutSessionNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoutineDayNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoutineDayNotFound(RoutineDayNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoutineExerciseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoutineExerciseNotFound(RoutineExerciseNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(WorkoutSessionAlreadyFinishedException.class)
    public ResponseEntity<Map<String, Object>> handleWorkoutSessionAlreadyFinished(WorkoutSessionAlreadyFinishedException ex) {
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
