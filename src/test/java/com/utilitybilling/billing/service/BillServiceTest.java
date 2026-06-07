package com.utilitybilling.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.utilitybilling.billing.dto.BillRequest;
import com.utilitybilling.billing.dto.BillResponse;
import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.billing.repository.BillRepository;
import com.utilitybilling.common.enums.AccountStatus;
import com.utilitybilling.common.enums.MeterStatus;
import com.utilitybilling.common.enums.MeterType;
import com.utilitybilling.common.enums.TariffStatus;
import com.utilitybilling.common.enums.TariffType;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.notification.service.EmailNotificationService;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.reading.entity.MeterReading;
import com.utilitybilling.reading.repository.MeterReadingRepository;
import com.utilitybilling.tariff.entity.Tariff;
import com.utilitybilling.tariff.entity.TariffTier;
import com.utilitybilling.tariff.service.TariffService;
import com.utilitybilling.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    private static final LocalDate FUTURE_DUE_DATE = LocalDate.now().plusDays(30);

    @Mock
    private BillRepository billRepository;

    @Mock
    private MeterReadingRepository meterReadingRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TariffService tariffService;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private BillService billService;

    @Test
    void generateBillRejectsDuplicateReadingBill() {
        MeterReading reading = buildReading(BigDecimal.valueOf(30), AccountStatus.ACTIVE);
        when(meterReadingRepository.findById(1L)).thenReturn(Optional.of(reading));
        when(billRepository.existsByReading(reading)).thenReturn(true);

        BillRequest request = new BillRequest(1L, FUTURE_DUE_DATE);

        assertThrows(BadRequestException.class, () -> billService.generateBill(request));
    }

    @Test
    void generateBillCalculatesFlatTariffTotals() {
        MeterReading reading = buildReading(BigDecimal.valueOf(30), AccountStatus.ACTIVE);
        Tariff tariff = buildFlatTariff();

        when(meterReadingRepository.findById(1L)).thenReturn(Optional.of(reading));
        when(billRepository.existsByReading(reading)).thenReturn(false);
        when(tariffService.findApplicableTariffEntity(MeterType.WATER, LocalDate.of(2026, 6, 1)))
            .thenReturn(tariff);
        when(billRepository.existsByBillReference(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setId(1L);
            return bill;
        });

        BillResponse response = billService.generateBill(new BillRequest(1L, FUTURE_DUE_DATE));

        assertEquals(BigDecimal.valueOf(15000.00).setScale(2), response.amountBeforeTax());
        assertEquals(BigDecimal.valueOf(2880.00).setScale(2), response.taxAmount());
        assertEquals(BigDecimal.valueOf(18880.00).setScale(2), response.totalAmount());
        verify(emailNotificationService).sendBillProcessedEmail(any(Bill.class));
    }

    @Test
    void generateBillCalculatesTieredTariffTotals() {
        MeterReading reading = buildReading(BigDecimal.valueOf(40), AccountStatus.ACTIVE);
        Tariff tariff = buildTieredTariff();

        when(meterReadingRepository.findById(1L)).thenReturn(Optional.of(reading));
        when(billRepository.existsByReading(reading)).thenReturn(false);
        when(tariffService.findApplicableTariffEntity(MeterType.WATER, LocalDate.of(2026, 6, 1)))
            .thenReturn(tariff);
        when(billRepository.existsByBillReference(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(billRepository.save(any(Bill.class))).thenAnswer(invocation -> {
            Bill bill = invocation.getArgument(0);
            bill.setId(2L);
            return bill;
        });

        BillResponse response = billService.generateBill(new BillRequest(1L, FUTURE_DUE_DATE));

        assertEquals(BigDecimal.valueOf(6000.00).setScale(2), response.amountBeforeTax());
        assertEquals(BigDecimal.valueOf(1260.00).setScale(2), response.taxAmount());
        assertEquals(BigDecimal.valueOf(8260.00).setScale(2), response.totalAmount());
        verify(emailNotificationService).sendBillProcessedEmail(any(Bill.class));
    }

    private MeterReading buildReading(BigDecimal consumption, AccountStatus customerStatus) {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFullName("Mugisha Prosper");
        customer.setEmail("prosper@example.com");
        customer.setStatus(customerStatus);

        Meter meter = new Meter();
        meter.setId(1L);
        meter.setMeterNumber("WTR-2026-001");
        meter.setMeterType(MeterType.WATER);
        meter.setStatus(MeterStatus.ACTIVE);
        meter.setCustomer(customer);

        MeterReading reading = new MeterReading();
        reading.setId(1L);
        reading.setMeter(meter);
        reading.setBillingMonth(6);
        reading.setBillingYear(2026);
        reading.setReadingDate(LocalDate.of(2026, 6, 5));
        reading.setConsumption(consumption.setScale(2));
        return reading;
    }

    private Tariff buildFlatTariff() {
        Tariff tariff = new Tariff();
        tariff.setMeterType(MeterType.WATER);
        tariff.setTariffType(TariffType.FLAT);
        tariff.setStatus(TariffStatus.ACTIVE);
        tariff.setRatePerUnit(BigDecimal.valueOf(500));
        tariff.setFixedCharge(BigDecimal.valueOf(1000));
        tariff.setVatPercentage(BigDecimal.valueOf(18));
        tariff.setPenaltyPercentage(BigDecimal.valueOf(5));
        return tariff;
    }

    private Tariff buildTieredTariff() {
        Tariff tariff = new Tariff();
        tariff.setMeterType(MeterType.WATER);
        tariff.setTariffType(TariffType.TIERED);
        tariff.setStatus(TariffStatus.ACTIVE);
        tariff.setFixedCharge(BigDecimal.valueOf(1000));
        tariff.setVatPercentage(BigDecimal.valueOf(18));
        tariff.setPenaltyPercentage(BigDecimal.valueOf(5));

        TariffTier tierOne = new TariffTier();
        tierOne.setMinUnits(BigDecimal.ZERO);
        tierOne.setMaxUnits(BigDecimal.TEN);
        tierOne.setRatePerUnit(BigDecimal.valueOf(100));

        TariffTier tierTwo = new TariffTier();
        tierTwo.setMinUnits(BigDecimal.TEN);
        tierTwo.setMaxUnits(BigDecimal.valueOf(30));
        tierTwo.setRatePerUnit(BigDecimal.valueOf(150));

        TariffTier tierThree = new TariffTier();
        tierThree.setMinUnits(BigDecimal.valueOf(30));
        tierThree.setMaxUnits(null);
        tierThree.setRatePerUnit(BigDecimal.valueOf(200));

        tariff.setTiers(List.of(tierOne, tierTwo, tierThree));
        return tariff;
    }
}
