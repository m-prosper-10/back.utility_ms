package com.utilitybilling.user.service;

import com.utilitybilling.common.enums.AccountStatus;
import com.utilitybilling.common.enums.RoleName;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.user.dto.UserCreateRequest;
import com.utilitybilling.user.dto.UserResponse;
import com.utilitybilling.user.entity.Role;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.RoleRepository;
import com.utilitybilling.user.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with this email already exists");
        }

        Set<Role> roles = resolveRoles(request.roles());
        Customer customer = resolveCustomerForRoles(request.customerId(), roles);

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRoles(roles);
        user.setCustomer(customer);

        return toResponse(userRepository.save(user));
    }

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        return requestedRoles.stream()
            .map(String::trim)
            .map(RoleName::valueOf)
            .map(name -> roleRepository.findByName(name)
                .orElseThrow(() -> new BadRequestException("Role not found: " + name)))
            .collect(java.util.stream.Collectors.toSet());
    }

    private Customer resolveCustomerForRoles(Long customerId, Set<Role> roles) {
        boolean hasCustomerRole = roles.stream().anyMatch(role -> role.getName() == RoleName.ROLE_CUSTOMER);
        if (!hasCustomerRole) {
            return null;
        }
        if (customerId == null) {
            throw new BadRequestException("Customer users must be linked to a customer record");
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found"));

        if (userRepository.existsByCustomer(customer)) {
            throw new BadRequestException("This customer already has a linked user account");
        }

        return customer;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getStatus(),
            user.getRoles().stream().map(role -> role.getName().name()).collect(java.util.stream.Collectors.toSet()),
            user.getCustomer() == null ? null : user.getCustomer().getId()
        );
    }
}
