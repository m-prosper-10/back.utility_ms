package com.utilitybilling.meter.repository;

import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.meter.entity.Meter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    boolean existsByMeterNumber(String meterNumber);

    List<Meter> findByCustomer(Customer customer);
}
