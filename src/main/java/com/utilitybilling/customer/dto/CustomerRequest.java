package com.utilitybilling.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
    @NotBlank(message = "Full name is required")
    String fullName,
    @NotBlank(message = "National ID is required")
    String nationalId,
    @Email(message = "Email must be valid")
    String email,
    @NotBlank(message = "Phone number is required")
    String phoneNumber,
    String address
) {
}
