package com.utilitybilling.billing.service;

import com.utilitybilling.billing.dto.BillRequest;
import com.utilitybilling.billing.dto.BillResponse;
import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.billing.repository.BillRepository;
import com.utilitybilling.common.enums.AccountStatus;
import com.utilitybilling.common.enums.BillStatus;
import com.utilitybilling.common.enums.TariffType;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.notification.service.EmailNotificationService;
import com.utilitybilling.notification.service.NotificationService;
import com.utilitybilling.reading.entity.MeterReading;
import com.utilitybilling.reading.repository.MeterReadingRepository;
import com.utilitybilling.tariff.entity.Tariff;
import com.utilitybilling.tariff.entity.TariffTier;
import com.utilitybilling.tariff.service.TariffService;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillService {

    private static final Logger log = LoggerFactory.getLogger(BillService.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final AtomicInteger BILL_SEQUENCE = new AtomicInteger(1);

    private final BillRepository billRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final TariffService tariffService;
    private final EmailNotificationService emailNotificationService;
    private final NotificationService notificationService;

    @Transactional
    public BillResponse generateBill(BillRequest request) {
        MeterReading reading = meterReadingRepository.findById(request.readingId())
            .orElseThrow(() -> new NotFoundException("Reading not found"));

        if (request.dueDate().isBefore(reading.getReadingDate())) {
            throw new BadRequestException("Due date cannot be before the reading date");
        }

        if (billRepository.existsByReading(reading)) {
            throw new BadRequestException("Bill has already been generated for this reading");
        }

        Customer customer = reading.getMeter().getCustomer();
        if (customer.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Inactive customers cannot receive bills");
        }

        LocalDate billingDate = LocalDate.of(reading.getBillingYear(), reading.getBillingMonth(), 1);
        Tariff tariff = tariffService.findApplicableTariffEntity(reading.getMeter().getMeterType(), billingDate);

        BigDecimal amountBeforeTax = calculateAmountBeforeTax(reading.getConsumption(), tariff);
        BigDecimal fixedCharge = scale(tariff.getFixedCharge());
        BigDecimal subtotal = amountBeforeTax.add(fixedCharge);
        BigDecimal penaltyAmount = calculatePenaltyAmount(subtotal, tariff, request.dueDate());
        BigDecimal taxAmount = scale(
            subtotal.multiply(tariff.getVatPercentage()).divide(HUNDRED, 2, RoundingMode.HALF_UP)
        );
        BigDecimal totalAmount = scale(subtotal.add(taxAmount).add(penaltyAmount));

        Bill bill = new Bill();
        bill.setBillReference(generateBillReference(reading));
        bill.setCustomer(customer);
        bill.setMeter(reading.getMeter());
        bill.setReading(reading);
        bill.setBillingMonth(reading.getBillingMonth());
        bill.setBillingYear(reading.getBillingYear());
        bill.setConsumption(scale(reading.getConsumption()));
        bill.setAmountBeforeTax(amountBeforeTax);
        bill.setTaxAmount(taxAmount);
        bill.setFixedCharge(fixedCharge);
        bill.setPenaltyAmount(penaltyAmount);
        bill.setTotalAmount(totalAmount);
        bill.setAmountPaid(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        bill.setOutstandingBalance(totalAmount);
        bill.setStatus(BillStatus.GENERATED);
        bill.setDueDate(request.dueDate());

        try {
            emailNotificationService.sendBillProcessedEmail(bill);
        } catch (RuntimeException ex) {
            log.warn("Bill processed email failed for bill {}", bill.getBillReference(), ex);
        }

        Bill savedBill = billRepository.saveAndFlush(bill);
        notificationService.ensureBillProcessedNotificationExists(savedBill);

        return toResponse(savedBill);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getAllBills() {
        return billRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id) {
        return toResponse(findBill(id));
    }

    @Transactional(readOnly = true)
    public BillResponse getBillByReference(String billReference) {
        return toResponse(
            billRepository.findByBillReference(billReference)
                .orElseThrow(() -> new NotFoundException("Bill not found"))
        );
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        return billRepository.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBillsForCurrentCustomer() {
        Customer customer = currentUser().getCustomer();
        if (customer == null) {
            throw new BadRequestException("Current user is not linked to a customer");
        }
        return billRepository.findByCustomer(customer).stream().map(this::toResponse).toList();
    }

    @Transactional
    public BillResponse approveBill(Long id) {
        Bill bill = findBill(id);
        if (bill.getStatus() != BillStatus.GENERATED) {
            throw new BadRequestException("Only generated bills can be approved");
        }

        User approver = currentUser();
        bill.setStatus(BillStatus.APPROVED);
        bill.setApprovedBy(approver);
        bill.setApprovedAt(LocalDateTime.now());

        return toResponse(billRepository.save(bill));
    }

    private Bill findBill(Long id) {
        return billRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Bill not found"));
    }

    private User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new NotFoundException("Current user not found"));
    }

    private BigDecimal calculateAmountBeforeTax(BigDecimal consumption, Tariff tariff) {
        if (tariff.getTariffType() == TariffType.FLAT) {
            return scale(consumption.multiply(tariff.getRatePerUnit()));
        }
        return calculateTieredAmount(consumption, tariff.getTiers());
    }

    private BigDecimal calculateTieredAmount(BigDecimal consumption, List<TariffTier> tiers) {
        BigDecimal total = BigDecimal.ZERO;
        List<TariffTier> sortedTiers = tiers.stream()
            .sorted(Comparator.comparing(TariffTier::getMinUnits))
            .toList();

        for (TariffTier tier : sortedTiers) {
            BigDecimal min = tier.getMinUnits();
            BigDecimal max = tier.getMaxUnits() == null ? consumption : tier.getMaxUnits();
            if (consumption.compareTo(min) > 0) {
                BigDecimal billableUnits = consumption.min(max).subtract(min);
                if (billableUnits.compareTo(BigDecimal.ZERO) > 0) {
                    total = total.add(billableUnits.multiply(tier.getRatePerUnit()));
                }
            }
        }

        return scale(total);
    }

    private BigDecimal calculatePenaltyAmount(BigDecimal subtotal, Tariff tariff, LocalDate dueDate) {
        if (!dueDate.isBefore(LocalDate.now())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return scale(
            subtotal.multiply(tariff.getPenaltyPercentage()).divide(HUNDRED, 2, RoundingMode.HALF_UP)
        );
    }

    private String generateBillReference(MeterReading reading) {
        int sequence = BILL_SEQUENCE.getAndIncrement();
        String reference = "BILL-%d-%02d-%04d".formatted(
            reading.getBillingYear(),
            reading.getBillingMonth(),
            sequence
        );
        while (billRepository.existsByBillReference(reference)) {
            sequence = BILL_SEQUENCE.getAndIncrement();
            reference = "BILL-%d-%02d-%04d".formatted(
                reading.getBillingYear(),
                reading.getBillingMonth(),
                sequence
            );
        }
        return reference;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BillResponse toResponse(Bill bill) {
        return new BillResponse(
            bill.getId(),
            bill.getBillReference(),
            bill.getCustomer().getId(),
            bill.getCustomer().getFullName(),
            bill.getMeter().getId(),
            bill.getMeter().getMeterNumber(),
            bill.getReading().getId(),
            bill.getBillingMonth(),
            bill.getBillingYear(),
            bill.getConsumption(),
            bill.getAmountBeforeTax(),
            bill.getTaxAmount(),
            bill.getFixedCharge(),
            bill.getPenaltyAmount(),
            bill.getTotalAmount(),
            bill.getAmountPaid(),
            bill.getOutstandingBalance(),
            bill.getStatus(),
            bill.getDueDate(),
            bill.getApprovedBy() == null ? null : bill.getApprovedBy().getEmail(),
            bill.getApprovedAt()
        );
    }
}
