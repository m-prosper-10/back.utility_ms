package com.utilitybilling.notification.service;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.common.enums.NotificationStatus;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.notification.dto.NotificationResponse;
import com.utilitybilling.notification.entity.Notification;
import com.utilitybilling.notification.repository.NotificationRepository;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification createBillProcessedNotification(Bill bill) {
        return createNotification(bill.getCustomer(), bill, buildBillProcessedMessage(bill));
    }

    @Transactional
    public Notification createFullPaymentNotification(Bill bill) {
        return createNotification(bill.getCustomer(), bill, buildFullPaymentMessage(bill));
    }

    @Transactional
    public void ensureFullPaymentNotificationExists(Bill bill) {
        String message = buildFullPaymentMessage(bill);
        if (notificationRepository.existsByBillAndMessage(bill, message)) {
            return;
        }
        createNotification(bill.getCustomer(), bill, message);
    }

    @Transactional
    public void ensureBillProcessedNotificationExists(Bill bill) {
        String message = buildBillProcessedMessage(bill);
        if (notificationRepository.existsByBillAndMessage(bill, message)) {
            return;
        }
        createNotification(bill.getCustomer(), bill, message);
    }

    public String buildBillProcessedMessage(Bill bill) {
        return "Dear %s,%nYour %02d/%d utility bill of %s FRW has been successfully processed."
            .formatted(
                bill.getCustomer().getFullName(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                formatAmount(bill.getTotalAmount())
            );
    }

    public String buildFullPaymentMessage(Bill bill) {
        return "Dear %s,%nYour %02d/%d utility bill of %s FRW has been fully paid."
            .formatted(
                bill.getCustomer().getFullName(),
                bill.getBillingMonth(),
                bill.getBillingYear(),
                formatAmount(bill.getTotalAmount())
            );
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

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsForCurrentCustomer() {
        Customer customer = currentUser().getCustomer();
        if (customer == null) {
            throw new NotFoundException("Current user is not linked to a customer");
        }
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

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new NotFoundException("Current user not found"));
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
