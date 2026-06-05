package com.utilitybilling.customer.dto;

import com.utilitybilling.common.enums.AccountStatus;

public record CustomerResponse(
    Long id,
    String fullName,
    String nationalId,
    String email,
    String phoneNumber,
    String address,
    AccountStatus status
) {
}
