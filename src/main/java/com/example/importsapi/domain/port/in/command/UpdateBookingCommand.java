package com.example.importsapi.domain.port.in.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBookingCommand(
        LocalDate issueDate,
        LocalDate expirationDate,
        BigDecimal fobValue,
        String currency
) {}
