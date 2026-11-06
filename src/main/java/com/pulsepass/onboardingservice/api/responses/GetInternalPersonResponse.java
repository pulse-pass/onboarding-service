package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.InternalLegalPersonResponse;
import com.pulsepass.onboardingservice.api.responses.definitions.NaturalPersonResponse;
import com.pulsepass.onboardingservice.api.responses.definitions.PersonPhoneNumberResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetInternalPersonDTO;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public record GetInternalPersonResponse(
        String personKey,
        String personType,
        String name,
        String documentNumber,
        Instant createdAt,
        List<PersonPhoneNumberResponse> phoneNumbers,
        InternalLegalPersonResponse legalPerson,
        NaturalPersonResponse naturalPerson
) {

    public static GetInternalPersonResponse fromDTO(GetInternalPersonDTO dto) {
        InternalLegalPersonResponse legalPerson = dto.legalPerson() == null
                ? null
                : InternalLegalPersonResponse.fromEntity(dto.legalPerson());
        NaturalPersonResponse naturalPerson = dto.naturalPerson() == null
                ? null
                : NaturalPersonResponse.fromEntity(dto.naturalPerson());
        return new GetInternalPersonResponse(
                dto.person().getPersonKey(),
                dto.person().getPersonType().getEnumerator(),
                dto.person().getName(),
                dto.person().getDocumentNumber(),
                dto.person().getCreatedAt().toInstant(ZoneOffset.UTC),
                dto.phoneNumbers().stream().map(PersonPhoneNumberResponse::fromEntity).toList(),
                legalPerson,
                naturalPerson
        );
    }
}
