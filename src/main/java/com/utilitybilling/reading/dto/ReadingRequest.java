package com.utilitybilling.reading.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ReadingRequest(
    @NotNull(message = "Meter ID is required")
    Long meterId,
    @NotNull(message = "Previous reading is required")
    BigDecimal previousReading,
    @NotNull(message = "Current reading is required")
    BigDecimal currentReading,
    @NotNull(message = "Reading date is required")
    LocalDate readingDate,
    @NotNull(message = "Billing month is required")
    @Min(value = 1, message = "Billing month must be between 1 and 12")
    @Max(value = 12, message = "Billing month must be between 1 and 12")
    Integer billingMonth,
    @NotNull(message = "Billing year is required")
    @Min(value = 2000, message = "Billing year must be valid")
    Integer billingYear
) {
}
