package com.utilitybilling.notification.service;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.billing.repository.BillRepository;
import com.utilitybilling.common.enums.BillStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillReminderService {

    private static final Logger log = LoggerFactory.getLogger(BillReminderService.class);

    private final BillRepository billRepository;
    private final EmailNotificationService emailNotificationService;

    @Value("${app.notifications.reminder.enabled:true}")
    private boolean reminderEnabled;

    @Value("${app.notifications.reminder.days-before:3}")
    private int reminderDaysBeforeDueDate;

    @Scheduled(cron = "${app.notifications.reminder.cron:0 25 16 * * *}")
    @Transactional
    public void sendDueDateReminders() {
        if (!reminderEnabled) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate reminderCutoffDate = today.plusDays(reminderDaysBeforeDueDate);

        List<Bill> bills = billRepository.findByStatusInAndOutstandingBalanceGreaterThanAndDueDateBetweenAndReminderSentAtIsNull(
            List.of(BillStatus.APPROVED, BillStatus.PARTIALLY_PAID),
            BigDecimal.ZERO,
            today,
            reminderCutoffDate
        );

        for (Bill bill : bills) {
            try {
                emailNotificationService.sendDueDateReminderEmail(bill);
                bill.setReminderSentAt(LocalDateTime.now());
            } catch (RuntimeException ex) {
                log.warn("Due-date reminder email failed for bill {}", bill.getBillReference(), ex);
            }
        }

        billRepository.saveAll(bills);
    }
}
