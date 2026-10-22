package com.pulsepass.onboardingservice.api.responses;

import com.pulsepass.onboardingservice.api.responses.definitions.LegalPersonResponse;
import com.pulsepass.onboardingservice.application.dtos.output.SessionPersonDTO;
import java.util.List;

public record SessionPersonResponse(
        String personKey,
        String name,
        String documentNumber,
        List<String> roles,
        LegalPersonResponse legalPerson
) {

    public static SessionPersonResponse fromDTO(SessionPersonDTO session) {
        LegalPersonResponse legalPerson = session.legalPerson() == null
                ? null
                : LegalPersonResponse.fromEntity(session.legalPerson());
        return new SessionPersonResponse(
                session.person().getPersonKey(),
                session.person().getName(),
                session.person().getDocumentNumber(),
                session.roles(),
                legalPerson
        );
    }
}
