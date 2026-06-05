package com.utilitybilling.auth.service;

import com.utilitybilling.auth.dto.AuthResponse;
import com.utilitybilling.auth.dto.LoginRequest;
import com.utilitybilling.auth.dto.SignupRequest;
import com.utilitybilling.auth.jwt.JwtService;
import com.utilitybilling.common.enums.AccountStatus;
import com.utilitybilling.common.enums.RoleName;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.NotFoundException;
import com.utilitybilling.common.exception.UnauthorizedException;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.user.entity.Role;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.RoleRepository;
import com.utilitybilling.user.repository.UserRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("User with this email already exists");
        }

        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new NotFoundException("Customer not found"));
        if (customer.getStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("Inactive customers cannot create portal accounts");
        }
        if (userRepository.existsByCustomer(customer)) {
            throw new BadRequestException("This customer already has a linked user account");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRoles(resolveCustomerRole());
        user.setCustomer(customer);

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("User account is inactive");
        }

        return buildAuthResponse(user);
    }

    private Set<Role> resolveCustomerRole() {
        return Set.of(roleRepository.findByName(RoleName.ROLE_CUSTOMER)
            .orElseThrow(() -> new BadRequestException("Role not found: ROLE_CUSTOMER")));
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        Set<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(java.util.stream.Collectors.toSet());
        return new AuthResponse(token, user.getEmail(), roles);
    }
}
