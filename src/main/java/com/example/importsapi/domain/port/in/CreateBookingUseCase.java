package com.example.importsapi.domain.port.in;

import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.port.in.command.CreateBookingCommand;

public interface CreateBookingUseCase {
    BookingRequest createBooking(CreateBookingCommand command);
}
