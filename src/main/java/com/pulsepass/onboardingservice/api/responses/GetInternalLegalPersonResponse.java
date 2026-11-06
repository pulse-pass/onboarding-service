package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonAddressResponse;
import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonOwnerResponse;
import com.pulsepass.onboardingservice.api.responses.definitions.PersonPhoneNumberResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetInternalLegalPersonDTO;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public record GetInternalLegalPersonResponse(
        String legalPersonKey,
        String name,
        String documentNumber,
        String email,
        List<PersonPhoneNumberResponse> phoneNumbers,
        String legalPersonCompanyType,
        BigDecimal monthlyRevenueAmount,
        LegalPersonAddressResponse legalPersonAddress,
        Instant createdAt,
        LegalPersonOwnerResponse owner
) {

    public static GetInternalLegalPersonResponse fromDTO(GetInternalLegalPersonDTO dto) {
        return new GetInternalLegalPersonResponse(
                dto.legalPerson().getPersonKey(),
                dto.legalPerson().getName(),
                dto.legalPerson().getDocumentNumber(),
                dto.legalPerson().getEmail(),
                dto.phoneNumbers().stream().map(PersonPhoneNumberResponse::fromEntity).toList(),
                dto.legalPersonCompanyType().getEnumerator(),
                dto.monthlyRevenueAmount(),
                LegalPersonAddressResponse.fromEntity(dto.legalPersonAddress()),
                dto.legalPerson().getCreatedAt().toInstant(ZoneOffset.UTC),
                LegalPersonOwnerResponse.fromEntity(dto.owner())
        );
    }
}
