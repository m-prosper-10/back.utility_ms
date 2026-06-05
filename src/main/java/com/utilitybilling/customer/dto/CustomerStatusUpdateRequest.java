package com.utilitybilling.customer.dto;

import com.utilitybilling.common.enums.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record CustomerStatusUpdateRequest(
    @NotNull(message = "Status is required")
    AccountStatus status
) {
}
