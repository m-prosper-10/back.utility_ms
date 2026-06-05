package com.utilitybilling.reading.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.utilitybilling.common.enums.MeterStatus;
import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.meter.repository.MeterRepository;
import com.utilitybilling.reading.dto.ReadingRequest;
import com.utilitybilling.reading.dto.ReadingResponse;
import com.utilitybilling.reading.entity.MeterReading;
import com.utilitybilling.reading.repository.MeterReadingRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadingServiceTest {

    @Mock
    private MeterReadingRepository meterReadingRepository;

    @Mock
    private MeterRepository meterRepository;

    @InjectMocks
    private ReadingService readingService;

    @Test
    void captureReadingRejectsInactiveMeter() {
        Meter meter = buildMeter(MeterStatus.INACTIVE);
        when(meterRepository.findById(1L)).thenReturn(Optional.of(meter));

        ReadingRequest request = new ReadingRequest(
            1L,
            BigDecimal.valueOf(120),
            BigDecimal.valueOf(150),
            LocalDate.now(),
            6,
            2026
        );

        assertThrows(BadRequestException.class, () -> readingService.captureReading(request));
    }

    @Test
    void captureReadingCalculatesConsumption() {
        Meter meter = buildMeter(MeterStatus.ACTIVE);
        when(meterRepository.findById(1L)).thenReturn(Optional.of(meter));
        when(meterReadingRepository.existsByMeterAndBillingMonthAndBillingYear(meter, 6, 2026))
            .thenReturn(false);
        when(meterReadingRepository.save(org.mockito.ArgumentMatchers.any(MeterReading.class)))
            .thenAnswer(invocation -> {
                MeterReading reading = invocation.getArgument(0);
                reading.setId(10L);
                return reading;
            });

        ReadingRequest request = new ReadingRequest(
            1L,
            BigDecimal.valueOf(120),
            BigDecimal.valueOf(150),
            LocalDate.of(2026, 6, 5),
            6,
            2026
        );

        ReadingResponse response = readingService.captureReading(request);

        assertEquals(BigDecimal.valueOf(30), response.consumption());
    }

    private Meter buildMeter(MeterStatus status) {
        Customer customer = new Customer();
        customer.setId(2L);
        customer.setFullName("Mugisha Prosper");

        Meter meter = new Meter();
        meter.setId(1L);
        meter.setMeterNumber("WTR-2026-001");
        meter.setMeterType(MeterType.WATER);
        meter.setStatus(status);
        meter.setCustomer(customer);
        return meter;
    }
}
