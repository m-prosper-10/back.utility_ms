package com.utilitybilling.user.dto;

import com.utilitybilling.common.enums.AccountStatus;
import java.util.Set;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    String phoneNumber,
    AccountStatus status,
    Set<String> roles,
    Long customerId
) {
}
