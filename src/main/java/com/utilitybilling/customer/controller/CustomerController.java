package com.utilitybilling.customer.controller;

import com.utilitybilling.common.response.ApiResponse;
import com.utilitybilling.customer.dto.CustomerRequest;
import com.utilitybilling.customer.dto.CustomerResponse;
import com.utilitybilling.customer.dto.CustomerStatusUpdateRequest;
import com.utilitybilling.customer.service.CustomerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
        @Valid @RequestBody CustomerRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Customer created successfully", customerService.createCustomer(request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(
            ApiResponse.success("Customers retrieved successfully", customerService.getAllCustomers())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Customer retrieved successfully", customerService.getCustomerById(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
        @PathVariable Long id,
        @Valid @RequestBody CustomerRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Customer updated successfully", customerService.updateCustomer(id, request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody CustomerStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Customer status updated successfully",
                customerService.updateStatus(id, request.status())
            )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success("Customer deleted successfully", null));
    }
}
