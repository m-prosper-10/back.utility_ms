package com.utilitybilling.meter.service;

import com.utilitybilling.common.enums.MeterStatus;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.meter.dto.MeterRequest;
import com.utilitybilling.meter.dto.MeterResponse;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.meter.repository.MeterRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public MeterResponse createMeter(MeterRequest request) {
        String meterNumber = request.meterNumber().trim();
        if (meterRepository.existsByMeterNumber(meterNumber)) {
            throw new BadRequestException("Meter with this number already exists");
        }

        Customer customer = findCustomer(request.customerId());
        Meter meter = new Meter();
        apply(meter, request, customer);
        meter.setMeterNumber(meterNumber);
        meter.setStatus(MeterStatus.ACTIVE);

        return toResponse(meterRepository.save(meter));
    }

    @Transactional(readOnly = true)
    public List<MeterResponse> getAllMeters() {
        return meterRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public MeterResponse getMeterById(Long id) {
        return toResponse(findMeter(id));
    }

    @Transactional(readOnly = true)
    public List<MeterResponse> getMetersByCustomer(Long customerId) {
        Customer customer = findCustomer(customerId);
        return meterRepository.findByCustomer(customer)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public MeterResponse updateMeter(Long id, MeterRequest request) {
        Meter meter = findMeter(id);
        String meterNumber = request.meterNumber().trim();

        if (!meter.getMeterNumber().equals(meterNumber)
            && meterRepository.existsByMeterNumber(meterNumber)) {
            throw new BadRequestException("Meter with this number already exists");
        }

        Customer customer = findCustomer(request.customerId());
        apply(meter, request, customer);
        meter.setMeterNumber(meterNumber);
        return toResponse(meterRepository.save(meter));
    }

    @Transactional
    public MeterResponse updateStatus(Long id, MeterStatus status) {
        Meter meter = findMeter(id);
        meter.setStatus(status);
        return toResponse(meterRepository.save(meter));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private Meter findMeter(Long id) {
        return meterRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Meter not found"));
    }

    private void apply(Meter meter, MeterRequest request, Customer customer) {
        meter.setCustomer(customer);
        meter.setMeterType(request.meterType());
        meter.setInstallationDate(request.installationDate());
    }

    private MeterResponse toResponse(Meter meter) {
        return new MeterResponse(
            meter.getId(),
            meter.getCustomer().getId(),
            meter.getCustomer().getFullName(),
            meter.getMeterNumber(),
            meter.getMeterType(),
            meter.getInstallationDate(),
            meter.getStatus()
        );
    }
}
