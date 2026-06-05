package com.utilitybilling.meter.dto;

import com.utilitybilling.common.enums.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MeterRequest(
    @NotNull(message = "Customer ID is required")
    Long customerId,
    @NotBlank(message = "Meter number is required")
    @Size(max = 50, message = "Meter number must not exceed 50 characters")
    String meterNumber,
    @NotNull(message = "Meter type is required")
    MeterType meterType,
    @NotNull(message = "Installation date is required")
    @PastOrPresent(message = "Installation date cannot be in the future")
    LocalDate installationDate
) {
}
