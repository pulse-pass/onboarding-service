package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonResponse;
import com.pulsepass.onboardingservice.api.responses.definitions.NaturalPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetPersonDTO;
import java.time.Instant;
import java.time.ZoneOffset;

public record GetPersonResponse(
        String personKey,
        String personType,
        String name,
        String documentNumber,
        Instant createdAt,
        LegalPersonResponse legalPerson,
        NaturalPersonResponse naturalPerson
) {

    public static GetPersonResponse fromDTO(GetPersonDTO dto) {
        LegalPersonResponse legalPerson = dto.legalPerson() == null
                ? null
                : LegalPersonResponse.fromEntity(dto.legalPerson());
        NaturalPersonResponse naturalPerson = dto.naturalPerson() == null
                ? null
                : NaturalPersonResponse.fromEntity(dto.naturalPerson());
        return new GetPersonResponse(
                dto.person().getPersonKey(),
                dto.person().getPersonType().getEnumerator(),
                dto.person().getName(),
                dto.person().getDocumentNumber(),
                dto.person().getCreatedAt().toInstant(ZoneOffset.UTC),
                legalPerson,
                naturalPerson
        );
    }
}
