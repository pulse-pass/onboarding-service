package com.pulsepass.onboardingservice.application.dtos.output;

import com.pulsepass.onboardingservice.domain.entities.LegalPersonAddress;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyType;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import java.math.BigDecimal;
import java.util.List;

public record GetInternalLegalPersonDTO(
        Person legalPerson,
        Person owner,
        List<PersonPhoneNumber> phoneNumbers,
        LegalPersonCompanyType legalPersonCompanyType,
        BigDecimal monthlyRevenueAmount,
        LegalPersonAddress legalPersonAddress
) {
}
