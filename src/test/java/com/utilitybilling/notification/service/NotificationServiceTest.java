package com.utilitybilling.notification.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.notification.entity.Notification;
import com.utilitybilling.notification.repository.NotificationRepository;
import com.utilitybilling.user.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void createBillProcessedNotificationBuildsExpectedMessage() {
        Bill bill = new Bill();
        Customer customer = new Customer();
        customer.setFullName("Mugisha Prosper");
        bill.setCustomer(customer);
        bill.setBillingMonth(6);
        bill.setBillingYear(2026);
        bill.setTotalAmount(BigDecimal.valueOf(15000));

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notification = notificationService.createBillProcessedNotification(bill);

        assertTrue(notification.getMessage().contains("Dear Mugisha Prosper"));
        assertTrue(notification.getMessage().contains("06/2026"));
        assertTrue(notification.getMessage().contains("15000"));
    }
}
