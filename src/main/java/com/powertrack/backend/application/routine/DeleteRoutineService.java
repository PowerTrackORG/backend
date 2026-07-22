package com.powertrack.backend.application.routine;

import com.powertrack.backend.application.routine.exception.RoutineNotFoundException;
import com.powertrack.backend.application.routine.port.in.DeleteRoutineUseCase;
import com.powertrack.backend.application.routine.port.out.RoutineRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteRoutineService implements DeleteRoutineUseCase {

    private final RoutineRepositoryPort routineRepository;

    public DeleteRoutineService(RoutineRepositoryPort routineRepository) {
        this.routineRepository = routineRepository;
    }

    @Override
    public void delete(UUID routineId, UUID userId) {
        boolean deleted = routineRepository.deleteByIdAndUserId(routineId, userId);
        if (!deleted) {
            throw new RoutineNotFoundException(routineId);
        }
    }
}
