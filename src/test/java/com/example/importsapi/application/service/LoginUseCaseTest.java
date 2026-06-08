package com.example.importsapi.application.service;

import com.example.importsapi.domain.exception.InvalidCredentialsException;
import com.example.importsapi.domain.model.User;
import com.example.importsapi.domain.port.in.command.LoginCommand;
import com.example.importsapi.domain.port.out.PasswordHasherPort;
import com.example.importsapi.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase")
class LoginUseCaseTest {

    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordHasherPort passwordHasher;
    @InjectMocks private AuthService authService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .id(1L)
                .username("juan")
                .password("$2a$10$hashedPassword")
                .taxId("12-3456789-0")
                .build();
    }

    @Test
    @DisplayName("retorna el taxId del proveedor cuando las credenciales son válidas")
    void login_success() {
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(validUser));
        when(passwordHasher.matches("secret123", validUser.getPassword())).thenReturn(true);

        String result = authService.login(new LoginCommand("juan", "secret123"));

        assertThat(result).isEqualTo("12-3456789-0");
    }

    @Test
    @DisplayName("lanza excepción cuando el usuario no existe")
    void login_userNotFound() {
        when(userRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("desconocido", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    @DisplayName("lanza excepción cuando la contraseña es incorrecta")
    void login_wrongPassword() {
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(validUser));
        when(passwordHasher.matches("incorrecta", validUser.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("juan", "incorrecta")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciales inválidas");
    }

    @Test
    @DisplayName("no distingue entre usuario inexistente y contraseña incorrecta")
    void login_sameExceptionForBothFailureCases() {
        when(userRepository.findByUsername("juan")).thenReturn(Optional.of(validUser));
        when(passwordHasher.matches("mal", validUser.getPassword())).thenReturn(false);
        when(userRepository.findByUsername("otro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginCommand("juan", "mal")))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThatThrownBy(() -> authService.login(new LoginCommand("otro", "secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
