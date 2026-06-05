package com.utilitybilling.reading.repository;

import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.reading.entity.MeterReading;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    boolean existsByMeterAndBillingMonthAndBillingYear(Meter meter, Integer billingMonth, Integer billingYear);

    List<MeterReading> findByMeter(Meter meter);

    List<MeterReading> findByBillingMonthAndBillingYear(Integer billingMonth, Integer billingYear);
}
