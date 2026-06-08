package com.example.importsapi.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class User {
    private Long id;
    private String username;
    private String password;
    private String taxId;
}
