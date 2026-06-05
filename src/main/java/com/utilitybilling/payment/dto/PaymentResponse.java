package com.utilitybilling.payment.dto;

import com.utilitybilling.common.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
    Long id,
    Long billId,
    String billReference,
    Long customerId,
    String customerName,
    BigDecimal amountPaid,
    PaymentMethod paymentMethod,
    LocalDate paymentDate
) {
}
