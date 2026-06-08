package com.example.importsapi.infrastructure.config;

import com.example.importsapi.domain.model.User;
import com.example.importsapi.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.hasAny()) return;

        userRepository.saveAll(List.of(
                User.builder().username("juan").password(passwordEncoder.encode("secret123")).taxId("12-3456789-0").build(),
                User.builder().username("maria").password(passwordEncoder.encode("secret123")).taxId("98-7654321-0").build(),
                User.builder().username("diego").password(passwordEncoder.encode("secret123")).taxId("76-5432198-0").build()
        ));
    }
}
