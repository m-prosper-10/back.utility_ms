package com.utilitybilling.reading.controller;

import com.utilitybilling.common.response.ApiResponse;
import com.utilitybilling.reading.dto.ReadingRequest;
import com.utilitybilling.reading.dto.ReadingResponse;
import com.utilitybilling.reading.service.ReadingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingController {

    private final ReadingService readingService;

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReadingResponse>> captureReading(
        @Valid @RequestBody ReadingRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Reading captured successfully", readingService.captureReading(request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReadingResponse>>> getAllReadings() {
        return ResponseEntity.ok(
            ApiResponse.success("Readings retrieved successfully", readingService.getAllReadings())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<ReadingResponse>>> getMonthlyReadings(
        @RequestParam Integer month,
        @RequestParam Integer year
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Monthly readings retrieved successfully",
                readingService.getMonthlyReadings(month, year)
            )
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'FINANCE')")
    @GetMapping("/api/meters/{meterId}/readings")
    public ResponseEntity<ApiResponse<List<ReadingResponse>>> getReadingsByMeter(
        @PathVariable Long meterId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Meter readings retrieved successfully",
                readingService.getReadingsByMeter(meterId)
            )
        );
    }
}
