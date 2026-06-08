package com.example.importsapi.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Supplier {
    private Long id;
    private String name;
    private String taxId;
    private String country;
    private String address;
    private String contactEmail;
}
