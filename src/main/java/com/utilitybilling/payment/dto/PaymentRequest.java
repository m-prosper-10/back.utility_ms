package com.utilitybilling.payment.dto;

import com.utilitybilling.common.enums.PaymentMethod;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
    @NotBlank(message = "Bill reference is required")
    @Size(max = 50, message = "Bill reference must not exceed 50 characters")
    String billReference,
    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    @Digits(integer = 12, fraction = 2, message = "Payment amount must have up to 12 integer digits and 2 decimals")
    BigDecimal amountPaid,
    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,
    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    LocalDate paymentDate
) {
}
