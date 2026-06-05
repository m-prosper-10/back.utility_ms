package com.utilitybilling.customer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.utilitybilling.common.enums.AccountStatus;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.customer.dto.CustomerRequest;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomerRejectsDuplicateNationalId() {
        CustomerRequest request = new CustomerRequest(
            "Mugisha Prosper",
            "1199980012345678",
            "prosper@example.com",
            "0788000000",
            "Kigali"
        );
        when(customerRepository.existsByNationalId("1199980012345678")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> customerService.createCustomer(request));
    }

    @Test
    void updateStatusUpdatesPersistedCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setStatus(AccountStatus.ACTIVE);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        customerService.updateStatus(1L, AccountStatus.INACTIVE);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertEquals(AccountStatus.INACTIVE, customerCaptor.getValue().getStatus());
    }
}
