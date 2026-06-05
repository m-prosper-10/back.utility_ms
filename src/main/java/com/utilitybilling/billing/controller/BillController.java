package com.utilitybilling.billing.controller;

import com.utilitybilling.billing.dto.BillRequest;
import com.utilitybilling.billing.dto.BillResponse;
import com.utilitybilling.billing.service.BillService;
import com.utilitybilling.common.response.ApiResponse;
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
public class BillController {

    private final BillService billService;

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @PostMapping("/api/bills/generate")
    public ResponseEntity<ApiResponse<BillResponse>> generateBill(
        @Valid @RequestBody BillRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Bill generated successfully", billService.generateBill(request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/bills")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAllBills() {
        return ResponseEntity.ok(
            ApiResponse.success("Bills retrieved successfully", billService.getAllBills())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/bills/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Bill retrieved successfully", billService.getBillById(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/bills/reference/{billReference}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillByReference(
        @PathVariable String billReference
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Bill retrieved successfully",
                billService.getBillByReference(billReference)
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/api/customers/{customerId}/bills")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBillsByCustomer(
        @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Customer bills retrieved successfully",
                billService.getBillsByCustomer(customerId)
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @PostMapping("/api/bills/{id}/approve")
    public ResponseEntity<ApiResponse<BillResponse>> approveBill(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Bill approved successfully", billService.approveBill(id))
        );
    }
}
