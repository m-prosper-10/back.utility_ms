package com.utilitybilling.tariff.dto;

import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TariffRequest(
    @NotNull(message = "Meter type is required")
    MeterType meterType,
    @NotNull(message = "Tariff type is required")
    TariffType tariffType,
    @DecimalMin(value = "0.00", inclusive = false, message = "Rate per unit must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Rate per unit must have up to 12 integer digits and 2 decimals")
    BigDecimal ratePerUnit,
    @NotNull(message = "Fixed charge is required")
    @DecimalMin(value = "0.00", message = "Fixed charge must be zero or greater")
    @Digits(integer = 12, fraction = 2, message = "Fixed charge must have up to 12 integer digits and 2 decimals")
    BigDecimal fixedCharge,
    @NotNull(message = "VAT percentage is required")
    @DecimalMin(value = "0.00", message = "VAT percentage must be zero or greater")
    @DecimalMax(value = "100.00", message = "VAT percentage must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "VAT percentage must have up to 3 integer digits and 2 decimals")
    BigDecimal vatPercentage,
    @NotNull(message = "Penalty percentage is required")
    @DecimalMin(value = "0.00", message = "Penalty percentage must be zero or greater")
    @DecimalMax(value = "100.00", message = "Penalty percentage must not exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Penalty percentage must have up to 3 integer digits and 2 decimals")
    BigDecimal penaltyPercentage,
    @NotNull(message = "Effective from date is required")
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    @Valid
    @Size(max = 20, message = "Tariff tiers must not exceed 20 entries")
    List<TariffTierRequest> tiers
) {
}
