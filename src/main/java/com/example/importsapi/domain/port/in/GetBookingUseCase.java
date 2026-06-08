package com.example.importsapi.domain.port.in;

import com.example.importsapi.domain.model.BookingRequest;

public interface GetBookingUseCase {
    BookingRequest getBooking(Long id, String callerTaxId);
}
