package com.example.importsapi.infrastructure.adapter.in.rest.dto.request;

import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateBookingRequest(
        @NotBlank @Size(max = 100) String bookingCode,
        @NotNull @JsonFormat(pattern = "dd-MM-yyyy") LocalDate issueDate,
        @NotNull @JsonFormat(pattern = "dd-MM-yyyy") LocalDate expirationDate,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull IncotermCode incotermCode,
        @NotNull FreightMode freightMode,
        @Size(max = 100) String originCountry,
        @Size(max = 100) String destinationCountry,
        @DecimalMin(value = "0.00", inclusive = false) BigDecimal fobValue,
        @NotNull @NotEmpty @Valid List<BookingItemRequest> items
) {}