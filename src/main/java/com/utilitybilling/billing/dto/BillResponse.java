package com.utilitybilling.billing.dto;

import com.utilitybilling.common.enums.BillStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BillResponse(
    Long id,
    String billReference,
    Long customerId,
    String customerName,
    Long meterId,
    String meterNumber,
    Long readingId,
    Integer billingMonth,
    Integer billingYear,
    BigDecimal consumption,
    BigDecimal amountBeforeTax,
    BigDecimal taxAmount,
    BigDecimal fixedCharge,
    BigDecimal penaltyAmount,
    BigDecimal totalAmount,
    BigDecimal amountPaid,
    BigDecimal outstandingBalance,
    BillStatus status,
    LocalDate dueDate,
    String approvedByEmail,
    LocalDateTime approvedAt
) {
}
