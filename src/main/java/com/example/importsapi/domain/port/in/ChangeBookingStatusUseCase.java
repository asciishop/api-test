package com.example.importsapi.domain.port.in;

import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.enums.BookingStatus;

public interface ChangeBookingStatusUseCase {
    BookingRequest changeStatus(Long id, BookingStatus newStatus, String callerTaxId);
}
