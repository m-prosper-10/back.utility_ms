package com.utilitybilling.reading.service;

import com.utilitybilling.common.enums.MeterStatus;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.meter.repository.MeterRepository;
import com.utilitybilling.reading.dto.ReadingRequest;
import com.utilitybilling.reading.dto.ReadingResponse;
import com.utilitybilling.reading.entity.MeterReading;
import com.utilitybilling.reading.repository.MeterReadingRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;

    @Transactional
    public ReadingResponse captureReading(ReadingRequest request) {
        Meter meter = findMeter(request.meterId());

        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BadRequestException("Inactive meter cannot receive readings");
        }

        if (request.currentReading().compareTo(request.previousReading()) <= 0) {
            throw new BadRequestException("Current reading must be greater than previous reading");
        }

        boolean exists = meterReadingRepository.existsByMeterAndBillingMonthAndBillingYear(
            meter,
            request.billingMonth(),
            request.billingYear()
        );
        if (exists) {
            throw new BadRequestException("Reading already exists for this meter in this billing cycle");
        }

        MeterReading reading = new MeterReading();
        reading.setMeter(meter);
        reading.setPreviousReading(request.previousReading());
        reading.setCurrentReading(request.currentReading());
        reading.setConsumption(request.currentReading().subtract(request.previousReading()));
        reading.setReadingDate(request.readingDate());
        reading.setBillingMonth(request.billingMonth());
        reading.setBillingYear(request.billingYear());

        return toResponse(meterReadingRepository.save(reading));
    }

    @Transactional(readOnly = true)
    public List<ReadingResponse> getAllReadings() {
        return meterReadingRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ReadingResponse> getReadingsByMeter(Long meterId) {
        Meter meter = findMeter(meterId);
        return meterReadingRepository.findByMeter(meter)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ReadingResponse> getMonthlyReadings(Integer month, Integer year) {
        return meterReadingRepository.findByBillingMonthAndBillingYear(month, year)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private Meter findMeter(Long id) {
        return meterRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Meter not found"));
    }

    private ReadingResponse toResponse(MeterReading reading) {
        return new ReadingResponse(
            reading.getId(),
            reading.getMeter().getId(),
            reading.getMeter().getMeterNumber(),
            reading.getPreviousReading(),
            reading.getCurrentReading(),
            reading.getConsumption(),
            reading.getReadingDate(),
            reading.getBillingMonth(),
            reading.getBillingYear()
        );
    }
}
