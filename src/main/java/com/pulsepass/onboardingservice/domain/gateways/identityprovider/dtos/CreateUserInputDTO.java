package com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos;

import java.util.List;

public record CreateUserInputDTO(
        String documentNumber,
        String name,
        String email,
        String password,
        String personKey,
        List<UserRoleInputDTO> roles
) {

    @Override
    public String toString() {
        return "CreateUserInputDTO[documentNumber=" + documentNumber
                + ", name=" + name
                + ", email=" + email
                + ", password=***"
                + ", personKey=" + personKey
                + ", roles=" + roles
                + "]";
    }
}
