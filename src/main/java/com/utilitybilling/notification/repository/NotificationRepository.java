package com.utilitybilling.notification.repository;

import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.notification.entity.Notification;
import com.utilitybilling.billing.entity.Bill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomer(Customer customer);

    boolean existsByBillAndMessage(Bill bill, String message);
}
