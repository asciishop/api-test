package com.example.importsapi.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BookingItem {
    private Long id;
    private String sku;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
}
