package com.example.importsapi.domain.exception;

public class DuplicateBookingCodeException extends RuntimeException {
    public DuplicateBookingCodeException(String bookingCode) {
        super("El código de reserva ya existe: " + bookingCode);
    }
}
