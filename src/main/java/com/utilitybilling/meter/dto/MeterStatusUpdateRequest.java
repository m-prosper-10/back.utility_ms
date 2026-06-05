package com.utilitybilling.meter.dto;

import com.utilitybilling.common.enums.MeterStatus;
import jakarta.validation.constraints.NotNull;

public record MeterStatusUpdateRequest(
    @NotNull(message = "Status is required")
    MeterStatus status
) {
}
