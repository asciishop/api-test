package com.example.importsapi.infrastructure.adapter.in.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BookingItemRequest(
        @NotBlank @Size(max = 100) String sku,
        @Size(max = 500) String description,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal unitPrice
) {}