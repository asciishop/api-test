package com.example.importsapi.domain.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long id) {
        super("Solicitud de reserva no encontrada con id: " + id);
    }
}
