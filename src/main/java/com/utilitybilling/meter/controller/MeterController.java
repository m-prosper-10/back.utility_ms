package com.utilitybilling.meter.controller;

import com.utilitybilling.common.response.ApiResponse;
import com.utilitybilling.meter.dto.MeterRequest;
import com.utilitybilling.meter.dto.MeterResponse;
import com.utilitybilling.meter.dto.MeterStatusUpdateRequest;
import com.utilitybilling.meter.service.MeterService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping("/api/meters")
    public ResponseEntity<ApiResponse<MeterResponse>> createMeter(
        @Valid @RequestBody MeterRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Meter created successfully", meterService.createMeter(request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/api/meters")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> getAllMeters() {
        return ResponseEntity.ok(
            ApiResponse.success("Meters retrieved successfully", meterService.getAllMeters())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/api/meters/{id}")
    public ResponseEntity<ApiResponse<MeterResponse>> getMeterById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Meter retrieved successfully", meterService.getMeterById(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @GetMapping("/api/customers/{customerId}/meters")
    public ResponseEntity<ApiResponse<List<MeterResponse>>> getMetersByCustomer(
        @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Customer meters retrieved successfully",
                meterService.getMetersByCustomer(customerId)
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping("/api/meters/{id}")
    public ResponseEntity<ApiResponse<MeterResponse>> updateMeter(
        @PathVariable Long id,
        @Valid @RequestBody MeterRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Meter updated successfully", meterService.updateMeter(id, request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PatchMapping("/api/meters/{id}/status")
    public ResponseEntity<ApiResponse<MeterResponse>> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody MeterStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Meter status updated successfully",
                meterService.updateStatus(id, request.status())
            )
        );
    }
}
