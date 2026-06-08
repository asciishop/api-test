package com.example.importsapi.domain.port.in;

import com.example.importsapi.domain.model.BookingRequest;
import com.example.importsapi.domain.model.PageResult;
import com.example.importsapi.domain.port.in.command.BookingFilterQuery;

public interface ListBookingsUseCase {
    PageResult<BookingRequest> listBookings(BookingFilterQuery query);
}
