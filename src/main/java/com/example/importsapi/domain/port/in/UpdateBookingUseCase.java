package com.example.importsapi.domain.port.in;

import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.port.in.command.UpdateBookingCommand;

public interface UpdateBookingUseCase {
    BookingRequest updateBooking(Long id, UpdateBookingCommand command, String callerTaxId);
}
