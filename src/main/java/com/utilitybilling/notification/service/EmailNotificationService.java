package com.utilitybilling.notification.service;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.customer.entity.Customer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.notifications.email.enabled:true}")
    private boolean emailEnabled;

    @Value("${app.notifications.email.from:${spring.mail.username:}}")
    private String fromAddress;

    public void sendBillProcessedEmail(Bill bill) {
        sendEmail(
            bill,
            "Utility bill processed - %s".formatted(bill.getBillReference()),
            "Dear %s,%nYour %02d/%d utility bill of %s FRW has been successfully processed."
                .formatted(
                    bill.getCustomer().getFullName(),
                    bill.getBillingMonth(),
                    bill.getBillingYear(),
                    formatAmount(bill.getTotalAmount())
                )
        );
    }

    public void sendFullPaymentEmail(Bill bill) {
        sendEmail(
            bill,
            "Utility bill fully paid - %s".formatted(bill.getBillReference()),
            "Dear %s,%nYour %02d/%d utility bill of %s FRW has been fully paid."
                .formatted(
                    bill.getCustomer().getFullName(),
                    bill.getBillingMonth(),
                    bill.getBillingYear(),
                    formatAmount(bill.getTotalAmount())
                )
        );
    }

    private void sendEmail(Bill bill, String subject, String message) {
        if (!emailEnabled) {
            return;
        }

        Customer customer = bill.getCustomer();
        if (customer == null || !StringUtils.hasText(customer.getEmail())) {
            throw new BadRequestException("Customer email is required for notification delivery");
        }
        if (!StringUtils.hasText(fromAddress)) {
            throw new IllegalStateException("Notification sender email is not configured");
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(customer.getEmail());
            helper.setSubject(subject);
            helper.setText(message, false);
            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Failed to prepare notification email", ex);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Failed to send notification email", ex);
        }
    }

    private String formatAmount(BigDecimal amount) {
        return new DecimalFormat("0.##").format(amount);
    }
}
