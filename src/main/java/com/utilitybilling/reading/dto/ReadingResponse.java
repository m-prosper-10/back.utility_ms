package com.utilitybilling.reading.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReadingResponse(
    Long id,
    Long meterId,
    String meterNumber,
    BigDecimal previousReading,
    BigDecimal currentReading,
    BigDecimal consumption,
    LocalDate readingDate,
    Integer billingMonth,
    Integer billingYear
) {
}
