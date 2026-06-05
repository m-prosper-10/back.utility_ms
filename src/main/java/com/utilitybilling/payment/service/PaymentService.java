package com.utilitybilling.payment.service;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.billing.repository.BillRepository;
import com.utilitybilling.common.enums.BillStatus;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.payment.dto.PaymentRequest;
import com.utilitybilling.payment.dto.PaymentResponse;
import com.utilitybilling.payment.entity.Payment;
import com.utilitybilling.payment.repository.PaymentRepository;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Bill bill = billRepository.findByBillReference(request.billReference())
            .orElseThrow(() -> new NotFoundException("Bill not found"));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill is already fully paid");
        }
        if (bill.getStatus() == BillStatus.GENERATED || bill.getStatus() == BillStatus.CANCELLED) {
            throw new BadRequestException("Only approved or partially paid bills can receive payments");
        }

        BigDecimal amountPaid = request.amountPaid().setScale(2, RoundingMode.HALF_UP);
        if (amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Payment amount must be greater than zero");
        }

        if (amountPaid.compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BadRequestException("Payment exceeds outstanding balance");
        }

        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setAmountPaid(amountPaid);
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentDate(request.paymentDate());

        BigDecimal newAmountPaid = bill.getAmountPaid().add(amountPaid).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newOutstanding = bill.getTotalAmount().subtract(newAmountPaid).setScale(2, RoundingMode.HALF_UP);

        bill.setAmountPaid(newAmountPaid);
        bill.setOutstandingBalance(newOutstanding);
        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.PAID);
        } else {
            bill.setStatus(BillStatus.PARTIALLY_PAID);
        }

        billRepository.save(bill);
        Payment savedPayment = paymentRepository.save(payment);

        return toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByBill(Long billId) {
        Bill bill = billRepository.findById(billId)
            .orElseThrow(() -> new NotFoundException("Bill not found"));
        return paymentRepository.findByBill(bill).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        return paymentRepository.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForCurrentCustomer() {
        Customer customer = currentUser().getCustomer();
        if (customer == null) {
            throw new BadRequestException("Current user is not linked to a customer");
        }
        return paymentRepository.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new NotFoundException("Current user not found"));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getBill().getId(),
            payment.getBill().getBillReference(),
            payment.getBill().getCustomer().getId(),
            payment.getBill().getCustomer().getFullName(),
            payment.getAmountPaid(),
            payment.getPaymentMethod(),
            payment.getPaymentDate()
        );
    }
}
