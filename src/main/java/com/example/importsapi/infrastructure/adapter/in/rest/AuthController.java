package com.example.importsapi.infrastructure.adapter.in.rest;

import com.example.importsapi.domain.port.in.LoginUseCase;
import com.example.importsapi.domain.port.in.command.LoginCommand;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.request.LoginRequest;
import com.example.importsapi.infrastructure.adapter.in.rest.dto.response.LoginResponse;
import com.example.importsapi.infrastructure.config.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String taxId = loginUseCase.login(new LoginCommand(request.username(), request.password()));
        return ResponseEntity.ok(new LoginResponse(jwtService.generateToken(taxId)));
    }
}
