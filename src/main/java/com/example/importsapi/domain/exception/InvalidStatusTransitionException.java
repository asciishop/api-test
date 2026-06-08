package com.example.importsapi.domain.exception;

import com.example.importsapi.domain.model.enums.BookingStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(BookingStatus from, BookingStatus to) {
        super("Transición de estado inválida de " + from + " a " + to);
    }
}
