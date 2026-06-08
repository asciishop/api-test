package com.example.importsapi.domain.port.in.command;

import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;

import java.time.LocalDate;

public record BookingFilterQuery(
        String taxId,
        BookingStatus status,
        FreightMode freightMode,
        LocalDate dateFrom,
        LocalDate dateTo,
        String bookingCode,
        int page,
        int size
) {}
