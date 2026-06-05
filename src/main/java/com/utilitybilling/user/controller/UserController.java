package com.utilitybilling.user.controller;

import com.utilitybilling.common.response.ApiResponse;
import com.utilitybilling.user.dto.UserCreateRequest;
import com.utilitybilling.user.dto.UserResponse;
import com.utilitybilling.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("User created successfully", userService.createUser(request))
        );
    }
}
