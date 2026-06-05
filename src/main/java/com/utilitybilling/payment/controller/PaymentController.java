package com.utilitybilling.payment.controller;

import com.utilitybilling.common.response.ApiResponse;
import com.utilitybilling.payment.dto.PaymentRequest;
import com.utilitybilling.payment.dto.PaymentResponse;
import com.utilitybilling.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasRole('FINANCE')")
    @PostMapping("/api/payments")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(
        @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Payment recorded successfully", paymentService.recordPayment(request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(
            ApiResponse.success("Payments retrieved successfully", paymentService.getAllPayments())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/bills/{billId}/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByBill(@PathVariable Long billId) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Bill payments retrieved successfully",
                paymentService.getPaymentsByBill(billId)
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/customers/{customerId}/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByCustomer(
        @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Customer payments retrieved successfully",
                paymentService.getPaymentsByCustomer(customerId)
            )
        );
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/api/customer/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getCurrentCustomerPayments() {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Customer payments retrieved successfully",
                paymentService.getPaymentsForCurrentCustomer()
            )
        );
    }
}
