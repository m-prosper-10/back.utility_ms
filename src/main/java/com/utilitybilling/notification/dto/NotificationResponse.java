package com.utilitybilling.notification.dto;

import com.utilitybilling.common.enums.NotificationStatus;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    Long customerId,
    String customerName,
    Long billId,
    String billReference,
    String message,
    NotificationStatus status,
    LocalDateTime createdAt
) {
}
