package com.utilitybilling.billing.repository;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.reading.entity.MeterReading;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {

    boolean existsByReading(MeterReading reading);

    boolean existsByBillReference(String billReference);

    Optional<Bill> findByBillReference(String billReference);

    List<Bill> findByCustomer(Customer customer);
}
