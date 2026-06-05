package com.utilitybilling.payment.repository;

import com.utilitybilling.billing.entity.Bill;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.payment.entity.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBill(Bill bill);

    @Query("""
        SELECT p FROM Payment p
        WHERE p.bill.customer = :customer
    """)
    List<Payment> findByCustomer(@Param("customer") Customer customer);
}
