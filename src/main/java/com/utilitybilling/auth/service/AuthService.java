package com.utilitybilling.auth.service;

import com.utilitybilling.auth.dto.AuthResponse;
import com.utilitybilling.auth.dto.LoginRequest;
import com.utilitybilling.auth.dto.SignupRequest;
import com.utilitybilling.auth.jwt.JwtService;
import com.utilitybilling.common.enums.AccountStatus;
import com.utilitybilling.common.enums.RoleName;
import com.utilitybilling.common.exception.BadRequestException;
import com.utilitybilling.common.exception.UnauthorizedException;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("User with this email already exists");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setStatus(AccountStatus.ACTIVE);
        user.setRoles(resolveRoles(request.roles()));

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

    private Set<Role> resolveRoles(Set<String> requestedRoles) {
        return requestedRoles.stream()
            .map(String::trim)
            .map(RoleName::valueOf)
            .map(name -> roleRepository.findByName(name)
                .orElseThrow(() -> new BadRequestException("Role not found: " + name)))
            .collect(java.util.stream.Collectors.toSet());
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        Set<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(java.util.stream.Collectors.toSet());
        return new AuthResponse(token, user.getEmail(), roles);
    }
}
