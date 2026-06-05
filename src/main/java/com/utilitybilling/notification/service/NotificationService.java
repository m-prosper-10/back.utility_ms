package com.utilitybilling.notification.service;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.common.enums.NotificationStatus;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.notification.dto.NotificationResponse;
import com.utilitybilling.notification.entity.Notification;
import com.utilitybilling.notification.repository.NotificationRepository;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public Notification createBillProcessedNotification(Bill bill) {
        String message = "Dear %s,%nYour %02d/%d utility bill of %s FRW has been successfully processed."
            .formatted(
                bill.getCustomer().getFullName(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                formatAmount(bill.getTotalAmount())
            );
        return createNotification(bill.getCustomer(), bill, message);
    }

    @Transactional
    public Notification createFullPaymentNotification(Bill bill) {
        String message = "Dear %s,%nYour %02d/%d utility bill of %s FRW has been fully paid."
            .formatted(
                bill.getCustomer().getFullName(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                formatAmount(bill.getTotalAmount())
            );
        return createNotification(bill.getCustomer(), bill, message);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        return notificationRepository.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificationResponse markSent(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Notification not found"));
        notification.setStatus(NotificationStatus.SENT);
        return toResponse(notificationRepository.save(notification));
    }

    private Notification createNotification(Customer customer, Bill bill, String message) {
        Notification notification = new Notification();
        notification.setCustomer(customer);
        notification.setBill(bill);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.PENDING);
        return notificationRepository.save(notification);
    }

    private String formatAmount(BigDecimal amount) {
        return new DecimalFormat("0.##").format(amount);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getCustomer().getId(),
            notification.getCustomer().getFullName(),
            notification.getBill() == null ? null : notification.getBill().getId(),
            notification.getBill() == null ? null : notification.getBill().getBillReference(),
            notification.getMessage(),
            notification.getStatus(),
            notification.getCreatedAt()
        );
    }
}
