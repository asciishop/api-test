package com.example.importsapi.domain.port.in.command;

import java.math.BigDecimal;

public record BookingItemData(
        String sku,
        String description,
        Integer quantity,
        BigDecimal unitPrice
) {}
