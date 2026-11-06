package com.pulsepass.onboardingservice.application.dtos.input;

import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonAddressDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonCollaboratorDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.PersonPhoneNumberDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCompanyTypeEnum;
import java.math.BigDecimal;
import java.util.List;

public record CreateLegalPersonDTO(
        String documentNumber,
        String name,
        String email,
        PersonPhoneNumberDTO phoneNumber,
        LegalPersonCompanyTypeEnum legalPersonCompanyType,
        BigDecimal monthlyRevenueAmount,
        LegalPersonAddressDTO legalPersonAddress,
        List<LegalPersonCollaboratorDTO> legalPersonCollaborators
) {
}
