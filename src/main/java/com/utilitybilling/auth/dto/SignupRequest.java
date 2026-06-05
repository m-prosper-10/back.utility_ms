package com.utilitybilling.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public record SignupRequest(
    @NotBlank(message = "Full name is required")
    String fullName,
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    String email,
    @NotBlank(message = "Phone number is required")
    String phoneNumber,
    @NotBlank(message = "Password is required")
    String password,
    @jakarta.validation.constraints.NotNull(message = "Customer ID is required")
    Long customerId
) {
}
