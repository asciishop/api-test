package com.example.importsapi.application.service;

import com.example.importsapi.domain.exception.InvalidCredentialsException;
import com.example.importsapi.domain.port.in.LoginUseCase;
import com.example.importsapi.domain.port.in.command.LoginCommand;
import com.example.importsapi.domain.port.out.PasswordHasherPort;
import com.example.importsapi.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    @Override
    public String login(LoginCommand command) {
        log.info("Intento de login para usuario: {}", command.username());
        return userRepository.findByUsername(command.username())
                .filter(user -> passwordHasher.matches(command.password(), user.getPassword()))
                .map(user -> {
                    log.info("Login exitoso para usuario: {}", command.username());
                    return user.getTaxId();
                })
                .orElseThrow(() -> {
                    log.warn("Login fallido para usuario: {}", command.username());
                    return new InvalidCredentialsException();
                });
    }
}
