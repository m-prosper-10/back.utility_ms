package com.utilitybilling.tariff.dto;

import java.math.BigDecimal;

public record TariffTierResponse(
    Long id,
    BigDecimal minUnits,
    BigDecimal maxUnits,
    BigDecimal ratePerUnit
) {
}
