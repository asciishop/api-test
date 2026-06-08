package com.example.importsapi.infrastructure.adapter.in.rest.dto.request;

import com.example.importsapi.domain.model.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull BookingStatus status
) {}
