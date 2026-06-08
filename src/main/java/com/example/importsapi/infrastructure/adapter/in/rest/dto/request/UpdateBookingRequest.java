package com.example.importsapi.infrastructure.adapter.in.rest.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBookingRequest(
        @JsonFormat(pattern = "dd-MM-yyyy") LocalDate issueDate,
        @JsonFormat(pattern = "dd-MM-yyyy") LocalDate expirationDate,
        @DecimalMin(value = "0.00", inclusive = false) BigDecimal fobValue,
        @Size(min = 3, max = 3) String currency
) {}