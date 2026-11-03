package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonOwnerResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetLegalPersonDTO;
import java.time.Instant;
import java.time.ZoneOffset;

public record GetLegalPersonResponse(
        String legalPersonKey,
        String name,
        String documentNumber,
        String email,
        Instant createdAt,
        LegalPersonOwnerResponse owner
) {

    public static GetLegalPersonResponse fromDTO(GetLegalPersonDTO dto) {
        return new GetLegalPersonResponse(
                dto.legalPerson().getPersonKey(),
                dto.legalPerson().getName(),
                dto.legalPerson().getDocumentNumber(),
                dto.legalPerson().getEmail(),
                dto.legalPerson().getCreatedAt().toInstant(ZoneOffset.UTC),
                LegalPersonOwnerResponse.fromEntity(dto.owner())
        );
    }
}
