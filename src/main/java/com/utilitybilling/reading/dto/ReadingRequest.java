package com.utilitybilling.reading.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ReadingRequest(
    @NotNull(message = "Meter ID is required")
    Long meterId,
    @NotNull(message = "Previous reading is required")
    @DecimalMin(value = "0.00", message = "Previous reading must be zero or greater")
    @Digits(integer = 12, fraction = 2, message = "Previous reading must have up to 12 integer digits and 2 decimals")
    BigDecimal previousReading,
    @NotNull(message = "Current reading is required")
    @DecimalMin(value = "0.00", message = "Current reading must be zero or greater")
    @Digits(integer = 12, fraction = 2, message = "Current reading must have up to 12 integer digits and 2 decimals")
    BigDecimal currentReading,
    @NotNull(message = "Reading date is required")
    @PastOrPresent(message = "Reading date cannot be in the future")
    LocalDate readingDate,
    @NotNull(message = "Billing month is required")
    @Min(value = 1, message = "Billing month must be between 1 and 12")
    @Max(value = 12, message = "Billing month must be between 1 and 12")
    Integer billingMonth,
    @NotNull(message = "Billing year is required")
    @Min(value = 2000, message = "Billing year must be valid")
    @Max(value = 2100, message = "Billing year must be valid")
    Integer billingYear
) {
}
