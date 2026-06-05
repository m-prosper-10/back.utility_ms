package com.utilitybilling.billing.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BillRequest(
    @NotNull(message = "Reading ID is required")
    Long readingId,
    @NotNull(message = "Due date is required")
    LocalDate dueDate
) {
}
