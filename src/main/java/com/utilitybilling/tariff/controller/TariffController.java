package com.utilitybilling.tariff.controller;

import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.response.ApiResponse;
import com.utilitybilling.tariff.dto.TariffRequest;
import com.utilitybilling.tariff.dto.TariffResponse;
import com.utilitybilling.tariff.service.TariffService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
public class TariffController {

    private final TariffService tariffService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<TariffResponse>> createTariff(
        @Valid @RequestBody TariffRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Tariff created successfully", tariffService.createTariff(request))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TariffResponse>>> getAllTariffs() {
        return ResponseEntity.ok(
            ApiResponse.success("Tariffs retrieved successfully", tariffService.getAllTariffs())
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TariffResponse>> getTariffById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Tariff retrieved successfully", tariffService.getTariffById(id))
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<TariffResponse>> getActiveTariff(
        @RequestParam MeterType meterType
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Active tariff retrieved successfully", tariffService.getActiveTariff(meterType))
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<TariffResponse>> deactivateTariff(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Tariff deactivated successfully", tariffService.deactivateTariff(id))
        );
    }
}
