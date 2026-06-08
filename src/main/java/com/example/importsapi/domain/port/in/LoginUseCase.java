package com.example.importsapi.domain.port.in;

import com.example.importsapi.domain.port.in.command.LoginCommand;

public interface LoginUseCase {
    String login(LoginCommand command);
}
