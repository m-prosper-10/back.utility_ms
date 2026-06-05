package com.utilitybilling.meter.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utilitybilling.common.enums.MeterStatus;
import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.meter.dto.MeterRequest;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.meter.repository.MeterRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeterServiceTest {

    @Mock
    private MeterRepository meterRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private MeterService meterService;

    @Test
    void createMeterRejectsDuplicateMeterNumber() {
        MeterRequest request = new MeterRequest(1L, "WTR-2026-001", MeterType.WATER, LocalDate.now());
        when(meterRepository.existsByMeterNumber("WTR-2026-001")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> meterService.createMeter(request));
    }

    @Test
    void updateStatusPersistsNewMeterStatus() {
        Meter meter = new Meter();
        meter.setId(1L);
        meter.setStatus(MeterStatus.ACTIVE);

        Customer customer = new Customer();
        customer.setId(2L);
        customer.setFullName("Mugisha Prosper");
        meter.setCustomer(customer);

        when(meterRepository.findById(1L)).thenReturn(Optional.of(meter));
        when(meterRepository.save(any(Meter.class))).thenAnswer(invocation -> invocation.getArgument(0));

        meterService.updateStatus(1L, MeterStatus.INACTIVE);

        ArgumentCaptor<Meter> meterCaptor = ArgumentCaptor.forClass(Meter.class);
        verify(meterRepository).save(meterCaptor.capture());
        assertEquals(MeterStatus.INACTIVE, meterCaptor.getValue().getStatus());
    }
}
