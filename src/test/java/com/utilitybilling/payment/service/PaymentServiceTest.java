package com.utilitybilling.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.billing.repository.BillRepository;
import com.utilitybilling.common.enums.BillStatus;
import com.utilitybilling.common.enums.PaymentMethod;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.notification.service.EmailNotificationService;
import com.utilitybilling.payment.dto.PaymentRequest;
import com.utilitybilling.payment.dto.PaymentResponse;
import com.utilitybilling.payment.entity.Payment;
import com.utilitybilling.payment.repository.PaymentRepository;
import com.utilitybilling.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BillRepository billRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void recordPaymentRejectsOverpayment() {
        Bill bill = buildBill();
        when(billRepository.findByBillReference("BILL-2026-06-0001")).thenReturn(Optional.of(bill));

        PaymentRequest request = new PaymentRequest(
            "BILL-2026-06-0001",
            BigDecimal.valueOf(25000),
            PaymentMethod.MOMO,
            LocalDate.of(2026, 6, 5)
        );

        assertThrows(BadRequestException.class, () -> paymentService.recordPayment(request));
    }

    @Test
    void recordPaymentMarksBillPaidAndCreatesNotification() {
        Bill bill = buildBill();
        when(billRepository.findByBillReference("BILL-2026-06-0001")).thenReturn(Optional.of(bill));
        when(paymentRepository.save(org.mockito.ArgumentMatchers.any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(1L);
            return payment;
        });

        PaymentRequest request = new PaymentRequest(
            "BILL-2026-06-0001",
            BigDecimal.valueOf(18880),
            PaymentMethod.MOMO,
            LocalDate.of(2026, 6, 5)
        );

        PaymentResponse response = paymentService.recordPayment(request);

        assertEquals(BillStatus.PAID, bill.getStatus());
        assertEquals(BigDecimal.ZERO.setScale(2), bill.getOutstandingBalance());
        assertEquals(BigDecimal.valueOf(18880.00).setScale(2), response.amountPaid());
        verify(emailNotificationService).sendFullPaymentEmail(bill);
    }

    @Test
    void recordPaymentMarksBillPartialWithoutNotification() {
        Bill bill = buildBill();
        when(billRepository.findByBillReference("BILL-2026-06-0001")).thenReturn(Optional.of(bill));
        when(paymentRepository.save(org.mockito.ArgumentMatchers.any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(2L);
            return payment;
        });

        PaymentRequest request = new PaymentRequest(
            "BILL-2026-06-0001",
            BigDecimal.valueOf(5000),
            PaymentMethod.CASH,
            LocalDate.of(2026, 6, 5)
        );

        paymentService.recordPayment(request);

        assertEquals(BillStatus.PARTIALLY_PAID, bill.getStatus());
        assertEquals(BigDecimal.valueOf(13880.00).setScale(2), bill.getOutstandingBalance());
        verify(emailNotificationService, never()).sendFullPaymentEmail(any(Bill.class));
    }

    private Bill buildBill() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Mugisha Prosper");
        customer.setEmail("prosper@example.com");

        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillReference("BILL-2026-06-0001");
        bill.setCustomer(customer);
        bill.setTotalAmount(BigDecimal.valueOf(18880.00).setScale(2));
        bill.setAmountPaid(BigDecimal.ZERO.setScale(2));
        bill.setOutstandingBalance(BigDecimal.valueOf(18880.00).setScale(2));
        bill.setStatus(BillStatus.APPROVED);
        bill.setCreatedAt(LocalDateTime.of(2026, 6, 5, 10, 0));
        return bill;
    }
}
