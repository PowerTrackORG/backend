package com.powertrack.backend.infrastructure.adapter.in.web.dto;

import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase.CreateRoutineCommand;
import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase.RoutineDayCommand;
import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase.RoutineExerciseCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;
import java.util.UUID;

public record CreateRoutineRequest(
        @NotBlank String name,
        String description,
        @NotEmpty @Valid List<RoutineDayRequest> days
) {

    public CreateRoutineCommand toCommand(UUID userId) {
        List<RoutineDayCommand> dayCommands = days.stream().map(RoutineDayRequest::toCommand).toList();
        return new CreateRoutineCommand(userId, name, description, dayCommands);
    }

    public record RoutineDayRequest(
            @NotBlank String dayName,
            @NotNull @PositiveOrZero Integer orderIndex,
            @NotEmpty @Valid List<RoutineExerciseRequest> exercises
    ) {
        public RoutineDayCommand toCommand() {
            List<RoutineExerciseCommand> exerciseCommands = exercises.stream()
                    .map(RoutineExerciseRequest::toCommand)
                    .toList();
            return new RoutineDayCommand(dayName, orderIndex, exerciseCommands);
        }
    }

    public record RoutineExerciseRequest(
            @NotNull UUID exerciseId,
            @NotNull @PositiveOrZero Integer orderIndex,
            @NotNull @Min(1) Integer targetSets,
            @NotNull @Min(1) Integer targetRepMin,
            @NotNull @Min(1) Integer targetRepMax,
            @PositiveOrZero Integer restSeconds,
            String notes
    ) {
        @AssertTrue(message = "targetRepMin debe ser menor o igual a targetRepMax")
        public boolean isRepRangeValid() {
            return targetRepMin == null || targetRepMax == null || targetRepMin <= targetRepMax;
        }

        public RoutineExerciseCommand toCommand() {
            return new RoutineExerciseCommand(exerciseId, orderIndex, targetSets, targetRepMin, targetRepMax, restSeconds, notes);
        }
    }
}
