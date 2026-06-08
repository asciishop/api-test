package com.example.importsapi.domain.exception;

public class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException(String taxId) {
        super("Proveedor no encontrado con taxId: " + taxId);
    }
}
