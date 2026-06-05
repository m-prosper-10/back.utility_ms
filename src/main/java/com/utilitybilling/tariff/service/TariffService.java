package com.utilitybilling.tariff.service;

import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffStatus;
import com.utilitybilling.common.enums.TariffType;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.tariff.dto.TariffRequest;
import com.utilitybilling.tariff.dto.TariffResponse;
import com.utilitybilling.tariff.dto.TariffTierRequest;
import com.utilitybilling.tariff.dto.TariffTierResponse;
import com.utilitybilling.tariff.entity.Tariff;
import com.utilitybilling.tariff.entity.TariffTier;
import com.utilitybilling.tariff.repository.TariffRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;

    @Transactional
    public TariffResponse createTariff(TariffRequest request) {
        validateTariffRequest(request);

        Tariff tariff = new Tariff();
        tariff.setMeterType(request.meterType());
        tariff.setTariffType(request.tariffType());
        tariff.setVersion(nextVersion(request.meterType()));
        tariff.setRatePerUnit(request.tariffType() == TariffType.FLAT ? request.ratePerUnit() : null);
        tariff.setFixedCharge(request.fixedCharge());
        tariff.setVatPercentage(request.vatPercentage());
        tariff.setPenaltyPercentage(request.penaltyPercentage());
        tariff.setEffectiveFrom(request.effectiveFrom());
        tariff.setEffectiveTo(request.effectiveTo());
        tariff.setStatus(TariffStatus.ACTIVE);

        if (request.tariffType() == TariffType.TIERED) {
            addTiers(tariff, request.tiers());
        }

        return toResponse(tariffRepository.save(tariff));
    }

    @Transactional(readOnly = true)
    public List<TariffResponse> getAllTariffs() {
        return tariffRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public TariffResponse getTariffById(Long id) {
        return toResponse(findTariff(id));
    }

    @Transactional(readOnly = true)
    public TariffResponse getActiveTariff(MeterType meterType) {
        LocalDate today = LocalDate.now();
        return tariffRepository.findApplicableTariffs(meterType, today)
            .stream()
            .findFirst()
            .map(this::toResponse)
            .orElseThrow(() -> new NotFoundException("No active tariff found for meter type"));
    }

    @Transactional(readOnly = true)
    public Tariff findApplicableTariffEntity(MeterType meterType, LocalDate billingDate) {
        return tariffRepository.findApplicableTariffs(meterType, billingDate)
            .stream()
            .findFirst()
            .orElseThrow(() -> new NotFoundException("No applicable tariff found"));
    }

    @Transactional
    public TariffResponse deactivateTariff(Long id) {
        Tariff tariff = findTariff(id);
        tariff.setStatus(TariffStatus.INACTIVE);
        if (tariff.getEffectiveTo() == null) {
            tariff.setEffectiveTo(LocalDate.now());
        }
        return toResponse(tariffRepository.save(tariff));
    }

    private void validateTariffRequest(TariffRequest request) {
        if (request.effectiveTo() != null && request.effectiveTo().isBefore(request.effectiveFrom())) {
            throw new BadRequestException("Effective to date cannot be before effective from date");
        }

        if (request.tariffType() == TariffType.FLAT) {
            if (request.ratePerUnit() == null || request.ratePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Flat tariff requires a positive rate per unit");
            }
            if (request.tiers() != null && !request.tiers().isEmpty()) {
                throw new BadRequestException("Flat tariff should not define tiers");
            }
            return;
        }

        if (request.ratePerUnit() != null) {
            throw new BadRequestException("Tiered tariff should not define a flat rate per unit");
        }
        if (request.tiers() == null || request.tiers().isEmpty()) {
            throw new BadRequestException("Tiered tariff requires at least one tier");
        }
        validateTiers(request.tiers());
    }

    private void validateTiers(List<TariffTierRequest> tiers) {
        List<TariffTierRequest> sortedTiers = tiers.stream()
            .sorted(Comparator.comparing(TariffTierRequest::minUnits))
            .toList();

        BigDecimal expectedMin = BigDecimal.ZERO;
        for (int i = 0; i < sortedTiers.size(); i++) {
            TariffTierRequest tier = sortedTiers.get(i);
            if (tier.minUnits().compareTo(expectedMin) != 0) {
                throw new BadRequestException("Tier ranges must be continuous and start at zero");
            }
            if (tier.maxUnits() != null && tier.maxUnits().compareTo(tier.minUnits()) <= 0) {
                throw new BadRequestException("Tier max units must be greater than min units");
            }
            if (i < sortedTiers.size() - 1 && tier.maxUnits() == null) {
                throw new BadRequestException("Only the last tier may have an open-ended max units");
            }
            expectedMin = tier.maxUnits() == null ? expectedMin : tier.maxUnits();
        }
    }

    private void addTiers(Tariff tariff, List<TariffTierRequest> tierRequests) {
        List<TariffTierRequest> sortedTiers = tierRequests.stream()
            .sorted(Comparator.comparing(TariffTierRequest::minUnits))
            .toList();

        for (TariffTierRequest tierRequest : sortedTiers) {
            TariffTier tier = new TariffTier();
            tier.setTariff(tariff);
            tier.setMinUnits(tierRequest.minUnits());
            tier.setMaxUnits(tierRequest.maxUnits());
            tier.setRatePerUnit(tierRequest.ratePerUnit());
            tariff.getTiers().add(tier);
        }
    }

    private int nextVersion(MeterType meterType) {
        return tariffRepository.findTopByMeterTypeOrderByVersionDesc(meterType)
            .map(tariff -> tariff.getVersion() + 1)
            .orElse(1);
    }

    private Tariff findTariff(Long id) {
        return tariffRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Tariff not found"));
    }

    private TariffResponse toResponse(Tariff tariff) {
        return new TariffResponse(
            tariff.getId(),
            tariff.getMeterType(),
            tariff.getTariffType(),
            tariff.getVersion(),
            tariff.getRatePerUnit(),
            tariff.getFixedCharge(),
            tariff.getVatPercentage(),
            tariff.getPenaltyPercentage(),
            tariff.getEffectiveFrom(),
            tariff.getEffectiveTo(),
            tariff.getStatus(),
            tariff.getTiers()
                .stream()
                .map(tier -> new TariffTierResponse(
                    tier.getId(),
                    tier.getMinUnits(),
                    tier.getMaxUnits(),
                    tier.getRatePerUnit()
                ))
                .toList()
        );
    }
}
