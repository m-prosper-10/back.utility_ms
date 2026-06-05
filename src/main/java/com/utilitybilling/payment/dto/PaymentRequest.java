package com.utilitybilling.payment.dto;

import com.utilitybilling.common.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
    @NotBlank(message = "Bill reference is required")
    String billReference,
    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    BigDecimal amountPaid,
    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,
    @NotNull(message = "Payment date is required")
    LocalDate paymentDate
) {
}
