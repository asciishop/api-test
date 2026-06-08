package com.example.importsapi.domain.port.in;

public interface DeleteBookingUseCase {
    void deleteBooking(Long id, String callerTaxId);
}
