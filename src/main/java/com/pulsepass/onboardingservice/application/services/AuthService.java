package com.pulsepass.onboardingservice.application.services;

import com.pulsepass.onboardingservice.application.dtos.input.CreateSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.DeleteSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.RefreshSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.output.SessionPersonDTO;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidCredentialsException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidSessionException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeleteSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.RefreshSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonRepository;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final PersonRepository personRepository;
    private final LegalPersonRepository legalPersonRepository;
    private final LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;
    private final IdentityProviderGateway identityProviderGateway;

    public AuthService(
            PersonRepository personRepository,
            LegalPersonRepository legalPersonRepository,
            LegalPersonCollaboratorRepository legalPersonCollaboratorRepository,
            IdentityProviderGateway identityProviderGateway
    ) {
        this.personRepository = personRepository;
        this.legalPersonRepository = legalPersonRepository;
        this.legalPersonCollaboratorRepository = legalPersonCollaboratorRepository;
        this.identityProviderGateway = identityProviderGateway;
    }

    public SessionOutputDTO createSession(CreateSessionDTO dto) {
        logger.info("AuthService.createSession - Starting");

        if (personRepository.findByDocumentNumber(dto.documentNumber()).isEmpty()) {
            logger.warn("AuthService.createSession - Person not found for CPF");
            throw new InvalidCredentialsException();
        }

        SessionOutputDTO session = identityProviderGateway.createSession(
                new CreateSessionInputDTO(dto.documentNumber(), dto.password())
        );
        logger.info("AuthService.createSession - Successfully finished");
        return session;
    }

    public SessionOutputDTO refreshSession(RefreshSessionDTO dto) {
        logger.info("AuthService.refreshSession - Starting");
        SessionOutputDTO session = identityProviderGateway.refreshSession(
                new RefreshSessionInputDTO(dto.refreshToken())
        );
        logger.info("AuthService.refreshSession - Successfully finished");
        return session;
    }

    public void deleteSession(DeleteSessionDTO dto) {
        logger.info("AuthService.deleteSession - Starting");
        identityProviderGateway.deleteSession(new DeleteSessionInputDTO(dto.refreshToken()));
        logger.info("AuthService.deleteSession - Successfully finished");
    }

    public SessionPersonDTO getSession(String personKey, String legalPersonKey, String roles) {
        logger.info("AuthService.getSession - Starting {}", Map.of("person_key", personKey));

        var person = personRepository.findByPersonKey(personKey).orElseThrow(() -> {
            logger.warn("AuthService.getSession - Person not found {}", Map.of("person_key", personKey));
            return new InvalidSessionException();
        });

        LegalPerson legalPerson = null;
        if (legalPersonKey != null) {
            legalPerson = legalPersonRepository.findByPersonKey(legalPersonKey).orElseThrow(() -> {
                logger.warn(
                        "AuthService.getSession - Legal person not found {}",
                        Map.of("person_key", personKey, "legal_person_key", legalPersonKey)
                );
                return new InvalidSessionException();
            });

            boolean collaborator = legalPersonCollaboratorRepository
                    .existsByLegalPersonKeyAndNaturalPersonKey(legalPersonKey, personKey);
            if (!collaborator) {
                logger.warn(
                        "AuthService.getSession - Person is not a collaborator of the legal person {}",
                        Map.of("person_key", personKey, "legal_person_key", legalPersonKey)
                );
                throw new InvalidSessionException();
            }
        }

        logger.info("AuthService.getSession - Successfully finished {}", Map.of("person_key", personKey));
        return new SessionPersonDTO(person, Arrays.asList(roles.split(",")), legalPerson);
    }
}
