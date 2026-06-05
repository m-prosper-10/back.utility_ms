package com.utilitybilling.tariff.dto;

import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffStatus;
import com.utilitybilling.common.enums.TariffType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TariffResponse(
    Long id,
    MeterType meterType,
    TariffType tariffType,
    Integer version,
    BigDecimal ratePerUnit,
    BigDecimal fixedCharge,
    BigDecimal vatPercentage,
    BigDecimal penaltyPercentage,
    LocalDate effectiveFrom,
    LocalDate effectiveTo,
    TariffStatus status,
    List<TariffTierResponse> tiers
) {
}
