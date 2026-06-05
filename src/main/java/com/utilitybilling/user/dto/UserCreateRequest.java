package com.utilitybilling.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record UserCreateRequest(
    @NotBlank(message = "Full name is required")
    String fullName,
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    String email,
    @NotBlank(message = "Phone number is required")
    String phoneNumber,
    @NotBlank(message = "Password is required")
    String password,
    @NotEmpty(message = "At least one role is required")
    Set<String> roles,
    Long customerId
) {
}
