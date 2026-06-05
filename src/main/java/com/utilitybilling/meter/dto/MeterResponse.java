package com.utilitybilling.meter.dto;

import com.utilitybilling.common.enums.MeterStatus;
import com.utilitybilling.common.enums.MeterType;
import java.time.LocalDate;

public record MeterResponse(
    Long id,
    Long customerId,
    String customerName,
    String meterNumber,
    MeterType meterType,
    LocalDate installationDate,
    MeterStatus status
) {
}
