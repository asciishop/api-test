package com.example.importsapi.infrastructure.adapter.in.rest.dto.response;

import java.math.BigDecimal;

public record BookingItemResponse(
        Long id,
        String sku,
        String description,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount
) {}
