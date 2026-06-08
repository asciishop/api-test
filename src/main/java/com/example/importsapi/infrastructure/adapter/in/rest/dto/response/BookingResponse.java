package com.example.importsapi.infrastructure.adapter.in.rest.dto.response;

import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        String bookingCode,
        LocalDate issueDate,
        LocalDate expirationDate,
        String currency,
        IncotermCode incotermCode,
        FreightMode freightMode,
        String originCountry,
        String destinationCountry,
        BigDecimal fobValue,
        BookingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        SupplierResponse supplier,
        List<BookingItemResponse> items
) {}
