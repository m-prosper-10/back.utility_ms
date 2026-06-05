package com.utilitybilling.user.repository;

import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByCustomer(Customer customer);

    Optional<User> findByEmail(String email);

    Optional<User> findByCustomer(Customer customer);
}
