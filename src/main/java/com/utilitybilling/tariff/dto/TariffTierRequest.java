package com.utilitybilling.tariff.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TariffTierRequest(
    @NotNull(message = "Minimum units are required")
    @DecimalMin(value = "0.00", message = "Minimum units must be zero or greater")
    @Digits(integer = 12, fraction = 2, message = "Minimum units must have up to 12 integer digits and 2 decimals")
    BigDecimal minUnits,
    @DecimalMin(value = "0.00", message = "Maximum units must be zero or greater")
    @Digits(integer = 12, fraction = 2, message = "Maximum units must have up to 12 integer digits and 2 decimals")
    BigDecimal maxUnits,
    @NotNull(message = "Tier rate per unit is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Tier rate per unit must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Tier rate per unit must have up to 12 integer digits and 2 decimals")
    BigDecimal ratePerUnit
) {
}
