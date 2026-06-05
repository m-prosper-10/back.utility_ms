package com.utilitybilling.tariff.dto;

import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TariffRequest(
    @NotNull(message = "Meter type is required")
    MeterType meterType,
    @NotNull(message = "Tariff type is required")
    TariffType tariffType,
    BigDecimal ratePerUnit,
    @NotNull(message = "Fixed charge is required")
    @DecimalMin(value = "0.00", message = "Fixed charge must be zero or greater")
    BigDecimal fixedCharge,
    @NotNull(message = "VAT percentage is required")
    @DecimalMin(value = "0.00", message = "VAT percentage must be zero or greater")
    BigDecimal vatPercentage,
    @NotNull(message = "Penalty percentage is required")
    @DecimalMin(value = "0.00", message = "Penalty percentage must be zero or greater")
    BigDecimal penaltyPercentage,
    @NotNull(message = "Effective from date is required")
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    @Valid
    List<TariffTierRequest> tiers
) {
}
