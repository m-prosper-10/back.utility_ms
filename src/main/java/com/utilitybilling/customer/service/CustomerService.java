package com.utilitybilling.customer.service;

import com.utilitybilling.common.enums.AccountStatus;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.customer.dto.CustomerRequest;
import com.utilitybilling.customer.dto.CustomerResponse;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        String nationalId = request.nationalId().trim();
        if (customerRepository.existsByNationalId(nationalId)) {
            throw new BadRequestException("Customer with this National ID already exists");
        }

        Customer customer = new Customer();
        apply(customer, request);
        customer.setNationalId(nationalId);
        customer.setStatus(AccountStatus.ACTIVE);

        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return toResponse(findCustomer(id));
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = findCustomer(id);
        String nationalId = request.nationalId().trim();

        if (!customer.getNationalId().equals(nationalId)
            && customerRepository.existsByNationalId(nationalId)) {
            throw new BadRequestException("Customer with this National ID already exists");
        }

        apply(customer, request);
        customer.setNationalId(nationalId);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public CustomerResponse updateStatus(Long id, AccountStatus status) {
        Customer customer = findCustomer(id);
        customer.setStatus(status);
        return toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = findCustomer(id);
        customerRepository.delete(customer);
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private void apply(Customer customer, CustomerRequest request) {
        customer.setFullName(request.fullName().trim());
        customer.setEmail(request.email() == null ? null : request.email().trim().toLowerCase());
        customer.setPhoneNumber(request.phoneNumber().trim());
        customer.setAddress(request.address());
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
            customer.getId(),
            customer.getFullName(),
            customer.getNationalId(),
            customer.getEmail(),
            customer.getPhoneNumber(),
            customer.getAddress(),
            customer.getStatus()
        );
    }
}
