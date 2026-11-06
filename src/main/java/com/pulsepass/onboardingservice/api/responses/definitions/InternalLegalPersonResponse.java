package com.pulsepass.onboardingservice.api.responses.definitions;

import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import java.math.BigDecimal;

public record InternalLegalPersonResponse(
        String email,
        String legalPersonCompanyType,
        BigDecimal monthlyRevenueAmount,
        LegalPersonAddressResponse legalPersonAddress
) {

    public static InternalLegalPersonResponse fromEntity(LegalPerson legalPerson) {
        return new InternalLegalPersonResponse(
                legalPerson.getEmail(),
                legalPerson.getLegalPersonCompanyType().getEnumerator(),
                legalPerson.getMonthlyRevenueAmount(),
                LegalPersonAddressResponse.fromEntity(legalPerson.getLegalPersonAddress())
        );
    }
}
