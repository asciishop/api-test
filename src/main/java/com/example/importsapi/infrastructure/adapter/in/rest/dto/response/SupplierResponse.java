package com.example.importsapi.infrastructure.adapter.in.rest.dto.response;

public record SupplierResponse(
        Long id,
        String name,
        String taxId,
        String country,
        String address,
        String contactEmail
) {}
