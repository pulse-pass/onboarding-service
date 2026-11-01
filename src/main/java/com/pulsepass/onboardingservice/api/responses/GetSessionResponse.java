package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.SessionLegalPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.GetSessionDTO;
import java.util.List;

public record GetSessionResponse(
        String personKey,
        String name,
        String documentNumber,
        List<String> roles,
        SessionLegalPersonResponse legalPerson
) {

    public static GetSessionResponse fromDTO(GetSessionDTO session) {
        SessionLegalPersonResponse legalPerson = session.legalPerson() == null
                ? null
                : SessionLegalPersonResponse.fromEntity(session.legalPerson());
        return new GetSessionResponse(
                session.person().getPersonKey(),
                session.person().getName(),
                session.person().getDocumentNumber(),
                session.roles(),
                legalPerson
        );
    }
}
