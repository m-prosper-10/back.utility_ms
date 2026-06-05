package com.utilitybilling.notification.controller;

import com.utilitybilling.common.response.ApiResponse;
import com.utilitybilling.notification.dto.NotificationResponse;
import com.utilitybilling.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAllNotifications() {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Notifications retrieved successfully",
                notificationService.getAllNotifications()
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE', 'CUSTOMER')")
    @GetMapping("/api/customers/{customerId}/notifications")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotificationsByCustomer(
        @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Customer notifications retrieved successfully",
                notificationService.getNotificationsByCustomer(customerId)
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @PatchMapping("/api/notifications/{id}/mark-sent")
    public ResponseEntity<ApiResponse<NotificationResponse>> markSent(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Notification marked as sent", notificationService.markSent(id))
        );
    }
}
