package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.exception.ExerciseNotFoundException;
import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase.CreateRoutineCommand;
import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase.RoutineDayCommand;
import com.powertrack.backend.application.routine.port.in.CreateRoutineUseCase.RoutineExerciseCommand;
import com.powertrack.backend.application.routine.port.in.RoutineDetailResult;
import com.powertrack.backend.application.routine.port.out.ExerciseRepositoryPort;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import com.powertrack.backend.domain.routine.Exercise;
import com.powertrack.backend.domain.routine.Routine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateRoutineServiceTest {

    @Mock
    private RoutineRepositoryPort routineRepository;
    @Mock
    private ExerciseRepositoryPort exerciseRepository;

    private CreateRoutineService service;

    @BeforeEach
    void setUp() {
        service = new CreateRoutineService(routineRepository, exerciseRepository);
    }

    @Test
    void creaUnaRutinaConDiasYEjerciciosYDevuelveElDetalleEnriquecidoConNombres() {
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();
        Exercise benchPress = Exercise.rehydrate(exerciseId, "Press banca", "Pecho", "Empuje", null);

        RoutineExerciseCommand exerciseCommand = new RoutineExerciseCommand(exerciseId, 0, 4, 8, 12, 90, "Cuidar la técnica");
        RoutineDayCommand dayCommand = new RoutineDayCommand("Día A", 0, List.of(exerciseCommand));
        CreateRoutineCommand command = new CreateRoutineCommand(userId, "Heavy Duty", "Rutina de fuerza", List.of(dayCommand));

        when(exerciseRepository.findAllByIds(List.of(exerciseId))).thenReturn(Map.of(exerciseId, benchPress));
        when(routineRepository.save(any(Routine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoutineDetailResult result = service.create(command);

        ArgumentCaptor<Routine> routineCaptor = ArgumentCaptor.forClass(Routine.class);
        verify(routineRepository).save(routineCaptor.capture());
        Routine savedRoutine = routineCaptor.getValue();

        assertThat(savedRoutine.getUserId()).isEqualTo(userId);
        assertThat(savedRoutine.getName()).isEqualTo("Heavy Duty");
        assertThat(savedRoutine.getDays()).hasSize(1);
        assertThat(savedRoutine.getDays().get(0).getExercises()).hasSize(1);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo("Heavy Duty");
        assertThat(result.days()).hasSize(1);
        assertThat(result.days().get(0).dayName()).isEqualTo("Día A");
        assertThat(result.days().get(0).exercises()).hasSize(1);
        assertThat(result.days().get(0).exercises().get(0).exerciseName()).isEqualTo("Press banca");
        assertThat(result.days().get(0).exercises().get(0).targetRepMin()).isEqualTo(8);
        assertThat(result.days().get(0).exercises().get(0).targetRepMax()).isEqualTo(12);
    }

    @Test
    void rechazaLaCreacionSiUnEjercicioReferenciadoNoExisteYNoGuardaNada() {
        UUID userId = UUID.randomUUID();
        UUID exerciseId = UUID.randomUUID();

        RoutineExerciseCommand exerciseCommand = new RoutineExerciseCommand(exerciseId, 0, 4, 8, 12, 90, null);
        RoutineDayCommand dayCommand = new RoutineDayCommand("Día A", 0, List.of(exerciseCommand));
        CreateRoutineCommand command = new CreateRoutineCommand(userId, "Heavy Duty", null, List.of(dayCommand));

        when(exerciseRepository.findAllByIds(List.of(exerciseId))).thenReturn(Map.of());

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(ExerciseNotFoundException.class);

        verify(routineRepository, never()).save(any());
    }
}
