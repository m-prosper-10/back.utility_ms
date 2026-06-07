package com.utilitybilling.notification.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.billing.repository.BillRepository;
import com.utilitybilling.common.enums.BillStatus;
import com.utilitybilling.customer.entity.Customer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BillReminderServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private BillReminderService billReminderService;

    @Test
    void sendDueDateRemindersSendsReminderForUpcomingBill() {
        Bill bill = buildBill();
        when(billRepository.findByStatusInAndOutstandingBalanceGreaterThanAndDueDateBetweenAndReminderSentAtIsNull(
            any(),
            any(),
            any(),
            any()
        )).thenReturn(List.of(bill));

        ReflectionTestUtils.setField(billReminderService, "reminderEnabled", true);
        ReflectionTestUtils.setField(billReminderService, "reminderDaysBeforeDueDate", 3);

        billReminderService.sendDueDateReminders();

        verify(emailNotificationService).sendDueDateReminderEmail(bill);
        verify(billRepository).saveAll(List.of(bill));
        assertNotNull(bill.getReminderSentAt());
    }

    @Test
    void sendDueDateRemindersSkipsWhenDisabled() {
        ReflectionTestUtils.setField(billReminderService, "reminderEnabled", false);

        billReminderService.sendDueDateReminders();

        verify(emailNotificationService, never()).sendDueDateReminderEmail(any(Bill.class));
    }

    private Bill buildBill() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Mugisha Prosper");
        customer.setEmail("prosper@example.com");

        Bill bill = new Bill();
        bill.setId(1L);
        bill.setCustomer(customer);
        bill.setBillingMonth(6);
        bill.setBillingYear(2026);
        bill.setDueDate(LocalDate.now().plusDays(2));
        bill.setOutstandingBalance(BigDecimal.valueOf(1000));
        bill.setStatus(BillStatus.APPROVED);
        return bill;
    }
}
