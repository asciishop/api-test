package com.example.importsapi.domain.port.in.command;

import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateBookingCommand(
        String bookingCode,
        LocalDate issueDate,
        LocalDate expirationDate,
        String currency,
        IncotermCode incotermCode,
        FreightMode freightMode,
        String originCountry,
        String destinationCountry,
        BigDecimal fobValue,
        String taxId,
        List<BookingItemData> items
) {}
