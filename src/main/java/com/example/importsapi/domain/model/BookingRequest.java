package com.example.importsapi.domain.model;

import com.example.importsapi.domain.model.enums.BookingStatus;
import com.example.importsapi.domain.model.enums.FreightMode;
import com.example.importsapi.domain.model.enums.IncotermCode;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class BookingRequest {
    private Long id;
    private String bookingCode;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private String currency;
    private IncotermCode incotermCode;
    private FreightMode freightMode;
    private String originCountry;
    private String destinationCountry;
    private BigDecimal fobValue;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Builder.Default
    private boolean active = true;
    private Supplier supplier;
    private List<BookingItem> items;
}
