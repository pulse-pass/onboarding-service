package com.pulsepass.onboardingservice.application.services;

import com.pulsepass.onboardingservice.application.dtos.input.CreateLegalPersonDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonCollaboratorDTO;
import com.pulsepass.onboardingservice.common.DocumentHandlers;
import com.pulsepass.onboardingservice.domain.UnitOfWork;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorHistory;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatusEnum;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
import com.pulsepass.onboardingservice.domain.entities.PersonTypeEnum;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidCollaboratorRoleException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidLegalPersonDocumentException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidNaturalPersonDocumentException;
import com.pulsepass.onboardingservice.domain.exceptions.LegalPersonAlreadyExistsException;
import com.pulsepass.onboardingservice.domain.exceptions.NaturalPersonAlreadyExistsException;
import com.pulsepass.onboardingservice.domain.exceptions.OrganizationCreationException;
import com.pulsepass.onboardingservice.domain.exceptions.UserCreationException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateUserInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.UserRoleInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.organization.OrganizationGateway;
import com.pulsepass.onboardingservice.domain.gateways.organization.dtos.CreateOrganizationInputDTO;
import com.pulsepass.onboardingservice.domain.repositories.CollaboratorRoleRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorHistoryRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorStatusRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.NaturalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonPhoneNumberRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonTypeRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class LegalPersonService {

    private static final Logger logger = LoggerFactory.getLogger(LegalPersonService.class);

    private final UnitOfWork unitOfWork;
    private final PersonRepository personRepository;
    private final PersonTypeRepository personTypeRepository;
    private final NaturalPersonRepository naturalPersonRepository;
    private final LegalPersonRepository legalPersonRepository;
    private final PersonPhoneNumberRepository personPhoneNumberRepository;
    private final CollaboratorRoleRepository collaboratorRoleRepository;
    private final LegalPersonCollaboratorStatusRepository legalPersonCollaboratorStatusRepository;
    private final LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;
    private final LegalPersonCollaboratorHistoryRepository legalPersonCollaboratorHistoryRepository;
    private final IdentityProviderGateway identityProviderGateway;
    private final OrganizationGateway organizationGateway;

    public LegalPersonService(
            UnitOfWork unitOfWork,
            PersonRepository personRepository,
            PersonTypeRepository personTypeRepository,
            NaturalPersonRepository naturalPersonRepository,
            LegalPersonRepository legalPersonRepository,
            PersonPhoneNumberRepository personPhoneNumberRepository,
            CollaboratorRoleRepository collaboratorRoleRepository,
            LegalPersonCollaboratorStatusRepository legalPersonCollaboratorStatusRepository,
            LegalPersonCollaboratorRepository legalPersonCollaboratorRepository,
            LegalPersonCollaboratorHistoryRepository legalPersonCollaboratorHistoryRepository,
            IdentityProviderGateway identityProviderGateway,
            OrganizationGateway organizationGateway
    ) {
        this.unitOfWork = unitOfWork;
        this.personRepository = personRepository;
        this.personTypeRepository = personTypeRepository;
        this.naturalPersonRepository = naturalPersonRepository;
        this.legalPersonRepository = legalPersonRepository;
        this.personPhoneNumberRepository = personPhoneNumberRepository;
        this.collaboratorRoleRepository = collaboratorRoleRepository;
        this.legalPersonCollaboratorStatusRepository = legalPersonCollaboratorStatusRepository;
        this.legalPersonCollaboratorRepository = legalPersonCollaboratorRepository;
        this.legalPersonCollaboratorHistoryRepository = legalPersonCollaboratorHistoryRepository;
        this.identityProviderGateway = identityProviderGateway;
        this.organizationGateway = organizationGateway;
    }

    private PersonType findPersonType(PersonTypeEnum personType) {
        return personTypeRepository
                .findByEnumerator(personType.name())
                .orElseThrow(() -> new IllegalStateException("Person type not found"));
    }

    private LegalPerson saveLegalPerson(CreateLegalPersonDTO dto, PersonType legalType) {
        Person person;
        try {
            person = personRepository.save(new Person(
                    UUID.randomUUID().toString(),
                    legalType,
                    dto.documentNumber(),
                    dto.name(),
                    dto.email()
            ));
        } catch (DataIntegrityViolationException e) {
            logger.warn("LegalPersonService.createLegalPerson - Unique violation on CNPJ", e);
            throw new LegalPersonAlreadyExistsException();
        }
        LegalPerson legalPerson = legalPersonRepository.save(new LegalPerson(person));
        personPhoneNumberRepository.save(new PersonPhoneNumber(
                UUID.randomUUID().toString(),
                person,
                dto.phoneNumber().countryCode(),
                dto.phoneNumber().areaCode(),
                dto.phoneNumber().number()
        ));
        return legalPerson;
    }

    private NaturalPerson saveNaturalPerson(LegalPersonCollaboratorDTO owner, PersonType naturalType) {
        Person person;
        try {
            person = personRepository.save(new Person(
                    UUID.randomUUID().toString(),
                    naturalType,
                    owner.documentNumber(),
                    owner.name(),
                    owner.email()
            ));
        } catch (DataIntegrityViolationException e) {
            logger.warn("LegalPersonService.createLegalPerson - Unique violation on CPF", e);
            throw new NaturalPersonAlreadyExistsException();
        }
        return naturalPersonRepository.save(new NaturalPerson(person));
    }

    private void createUser(LegalPersonCollaboratorDTO owner, String legalPersonKey, String ownerKey) {
        try {
            identityProviderGateway.createUser(new CreateUserInputDTO(
                    owner.documentNumber(),
                    owner.name(),
                    owner.email(),
                    owner.password(),
                    ownerKey,
                    legalPersonKey,
                    List.of(UserRoleInputDTO.ORGANIZER)
            ));
        } catch (UserCreationException ex) {
            logger.error(
                    "LegalPersonService.createLegalPerson - Failed to create the user {}",
                    Map.of("legal_person_key", legalPersonKey, "owner_person_key", ownerKey),
                    ex
            );
            throw ex;
        }
    }

    private void createOrganization(CreateLegalPersonDTO dto, String legalPersonKey, String ownerKey) {
        try {
            organizationGateway.createOrganization(new CreateOrganizationInputDTO(
                    legalPersonKey,
                    dto.documentNumber(),
                    dto.name()
            ));
        } catch (OrganizationCreationException ex) {
            logger.error(
                    "LegalPersonService.createLegalPerson - Failed to create the organization {}",
                    Map.of("legal_person_key", legalPersonKey, "owner_person_key", ownerKey),
                    ex
            );
            throw ex;
        }
    }

    public LegalPerson createLegalPerson(CreateLegalPersonDTO dto) {
        logger.info("LegalPersonService.createLegalPerson - Starting");

        LegalPersonCollaboratorDTO owner = dto.legalPersonCollaborators().get(0);

        if (!DocumentHandlers.isValidCnpj(dto.documentNumber())) {
            logger.warn("LegalPersonService.createLegalPerson - Invalid CNPJ");
            throw new InvalidLegalPersonDocumentException();
        }

        if (!CollaboratorRoleEnum.OWNER.name().equals(owner.collaboratorRole())) {
            logger.warn("LegalPersonService.createLegalPerson - Collaborator role is not OWNER");
            throw new InvalidCollaboratorRoleException();
        }

        if (!DocumentHandlers.isValidCpf(owner.documentNumber())) {
            logger.warn("LegalPersonService.createLegalPerson - Invalid CPF");
            throw new InvalidNaturalPersonDocumentException();
        }

        if (personRepository.findByDocumentNumber(dto.documentNumber()).isPresent()) {
            logger.warn("LegalPersonService.createLegalPerson - Legal person already exists for CNPJ");
            throw new LegalPersonAlreadyExistsException();
        }

        if (personRepository.findByDocumentNumber(owner.documentNumber()).isPresent()) {
            logger.warn("LegalPersonService.createLegalPerson - Natural person already exists for CPF");
            throw new NaturalPersonAlreadyExistsException();
        }

        PersonType legalType = findPersonType(PersonTypeEnum.LEGAL);
        PersonType naturalType = findPersonType(PersonTypeEnum.NATURAL);
        CollaboratorRole ownerRole = collaboratorRoleRepository
                .findByEnumerator(CollaboratorRoleEnum.OWNER.name())
                .orElseThrow(() -> new IllegalStateException("Collaborator role OWNER not found"));
        LegalPersonCollaboratorStatus activeStatus = legalPersonCollaboratorStatusRepository
                .findByEnumerator(LegalPersonCollaboratorStatusEnum.ACTIVE.name())
                .orElseThrow(() -> new IllegalStateException("Legal person collaborator status ACTIVE not found"));

        unitOfWork.begin();
        LegalPerson legalPerson = saveLegalPerson(dto, legalType);
        NaturalPerson naturalPerson = saveNaturalPerson(owner, naturalType);
        LegalPersonCollaborator collaborator = legalPersonCollaboratorRepository.save(new LegalPersonCollaborator(
                UUID.randomUUID().toString(),
                legalPerson,
                naturalPerson,
                ownerRole,
                activeStatus
        ));
        legalPersonCollaboratorHistoryRepository.save(
                LegalPersonCollaboratorHistory.fromLegalPersonCollaborator(collaborator, null)
        );
        unitOfWork.commit();

        String legalPersonKey = legalPerson.getPerson().getPersonKey();
        String ownerKey = naturalPerson.getPerson().getPersonKey();
        createUser(owner, legalPersonKey, ownerKey);
        createOrganization(dto, legalPersonKey, ownerKey);

        logger.info("LegalPersonService.createLegalPerson - Successfully finished");
        return legalPerson;
    }
}
