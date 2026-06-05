package com.utilitybilling.meter.dto;

import com.utilitybilling.common.enums.MeterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MeterRequest(
    @NotNull(message = "Customer ID is required")
    Long customerId,
    @NotBlank(message = "Meter number is required")
    String meterNumber,
    @NotNull(message = "Meter type is required")
    MeterType meterType,
    @NotNull(message = "Installation date is required")
    LocalDate installationDate
) {
}
