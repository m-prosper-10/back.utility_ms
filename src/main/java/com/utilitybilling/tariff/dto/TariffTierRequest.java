package com.utilitybilling.tariff.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TariffTierRequest(
    @NotNull(message = "Minimum units are required")
    @DecimalMin(value = "0.00", message = "Minimum units must be zero or greater")
    BigDecimal minUnits,
    BigDecimal maxUnits,
    @NotNull(message = "Tier rate per unit is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Tier rate per unit must be greater than zero")
    BigDecimal ratePerUnit
) {
}
