package com.powertrack.backend.infrastructure.adapter.in.web;

import com.powertrack.backend.application.auth.port.in.AuthenticateUserUseCase;
import com.powertrack.backend.application.auth.port.in.AuthenticateUserUseCase.LoginCommand;
import com.powertrack.backend.application.auth.port.in.RegisterUserUseCase;
import com.powertrack.backend.application.auth.port.in.RegisterUserUseCase.RegisterCommand;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.AuthResponse;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.LoginRequest;
import com.powertrack.backend.infrastructure.adapter.in.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUserUseCase.register(
                new RegisterCommand(request.email(), request.password(), request.fullName(), request.sportGoal()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUserUseCase.authenticate(new LoginCommand(request.email(), request.password()));
        return ResponseEntity.ok(AuthResponse.from(result));
    }
}
