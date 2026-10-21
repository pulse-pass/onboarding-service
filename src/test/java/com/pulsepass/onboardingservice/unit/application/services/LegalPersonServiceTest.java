package com.pulsepass.onboardingservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.application.dtos.input.CreateLegalPersonDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.LegalPersonCollaboratorDTO;
import com.pulsepass.onboardingservice.application.dtos.input.definitions.PersonPhoneNumberDTO;
import com.pulsepass.onboardingservice.application.services.LegalPersonService;
import com.pulsepass.onboardingservice.domain.UnitOfWork;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRole;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorHistory;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaboratorStatus;
import com.pulsepass.onboardingservice.domain.entities.NaturalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.entities.PersonPhoneNumber;
import com.pulsepass.onboardingservice.domain.entities.PersonType;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class LegalPersonServiceTest {

    private static final String CNPJ = "12345678000195";
    private static final String CPF = "52998224725";

    @Mock
    private UnitOfWork unitOfWork;
    @Mock
    private PersonRepository personRepository;
    @Mock
    private PersonTypeRepository personTypeRepository;
    @Mock
    private NaturalPersonRepository naturalPersonRepository;
    @Mock
    private LegalPersonRepository legalPersonRepository;
    @Mock
    private PersonPhoneNumberRepository personPhoneNumberRepository;
    @Mock
    private CollaboratorRoleRepository collaboratorRoleRepository;
    @Mock
    private LegalPersonCollaboratorStatusRepository legalPersonCollaboratorStatusRepository;
    @Mock
    private LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;
    @Mock
    private LegalPersonCollaboratorHistoryRepository legalPersonCollaboratorHistoryRepository;
    @Mock
    private IdentityProviderGateway identityProviderGateway;
    @Mock
    private OrganizationGateway organizationGateway;

    private LegalPersonService service;

    private final PersonType legalType = new PersonType(1L, "LEGAL", null);
    private final PersonType naturalType = new PersonType(2L, "NATURAL", null);
    private final CollaboratorRole ownerRole = new CollaboratorRole(1L, "OWNER", null);
    private final LegalPersonCollaboratorStatus activeStatus = new LegalPersonCollaboratorStatus(1L, "ACTIVE", null);

    @BeforeEach
    void setUp() {
        service = new LegalPersonService(
                unitOfWork,
                personRepository,
                personTypeRepository,
                naturalPersonRepository,
                legalPersonRepository,
                personPhoneNumberRepository,
                collaboratorRoleRepository,
                legalPersonCollaboratorStatusRepository,
                legalPersonCollaboratorRepository,
                legalPersonCollaboratorHistoryRepository,
                identityProviderGateway,
                organizationGateway
        );
    }

    private static CreateLegalPersonDTO dto(String cnpj, String role, String cpf) {
        return new CreateLegalPersonDTO(
                cnpj,
                "Acme Events",
                "acme@mail.com",
                new PersonPhoneNumberDTO("55", "11", "999999999"),
                List.of(new LegalPersonCollaboratorDTO(role, cpf, "John Doe", "john@mail.com", "Secret123"))
        );
    }

    private void stubNoDuplicates() {
        when(personRepository.findByDocumentNumber(CNPJ)).thenReturn(Optional.empty());
        when(personRepository.findByDocumentNumber(CPF)).thenReturn(Optional.empty());
    }

    private void stubPersonTypes() {
        when(personTypeRepository.findByEnumerator("LEGAL")).thenReturn(Optional.of(legalType));
        when(personTypeRepository.findByEnumerator("NATURAL")).thenReturn(Optional.of(naturalType));
    }

    private void stubOwnerRole() {
        when(collaboratorRoleRepository.findByEnumerator("OWNER")).thenReturn(Optional.of(ownerRole));
    }

    private void stubActiveStatus() {
        when(legalPersonCollaboratorStatusRepository.findByEnumerator("ACTIVE"))
                .thenReturn(Optional.of(activeStatus));
    }

    private void stubLookups() {
        stubNoDuplicates();
        stubPersonTypes();
        stubOwnerRole();
        stubActiveStatus();
    }

    private void stubSaves() {
        when(personRepository.save(any(Person.class))).thenAnswer(inv -> inv.getArgument(0));
        when(legalPersonRepository.save(any(LegalPerson.class))).thenAnswer(inv -> inv.getArgument(0));
        when(naturalPersonRepository.save(any(NaturalPerson.class))).thenAnswer(inv -> inv.getArgument(0));
        when(legalPersonCollaboratorRepository.save(any(LegalPersonCollaborator.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createLegalPerson_validRequest_persistsEverythingAndCallsGateways() {
        stubLookups();
        stubSaves();

        LegalPerson result = service.createLegalPerson(dto(CNPJ, "OWNER", CPF));

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository, times(2)).save(personCaptor.capture());
        Person legalPersonSaved = personCaptor.getAllValues().get(0);
        Person ownerSaved = personCaptor.getAllValues().get(1);
        assertThat(legalPersonSaved.getPersonType()).isSameAs(legalType);
        assertThat(legalPersonSaved.getDocumentNumber()).isEqualTo(CNPJ);
        assertThat(legalPersonSaved.getName()).isEqualTo("Acme Events");
        assertThat(legalPersonSaved.getEmail()).isEqualTo("acme@mail.com");
        assertThat(UUID.fromString(legalPersonSaved.getPersonKey())).isNotNull();
        assertThat(ownerSaved.getPersonType()).isSameAs(naturalType);
        assertThat(ownerSaved.getDocumentNumber()).isEqualTo(CPF);
        assertThat(ownerSaved.getName()).isEqualTo("John Doe");
        assertThat(ownerSaved.getEmail()).isEqualTo("john@mail.com");
        assertThat(UUID.fromString(ownerSaved.getPersonKey())).isNotNull();
        assertThat(ownerSaved.getPersonKey()).isNotEqualTo(legalPersonSaved.getPersonKey());

        ArgumentCaptor<LegalPerson> legalCaptor = ArgumentCaptor.forClass(LegalPerson.class);
        verify(legalPersonRepository).save(legalCaptor.capture());
        assertThat(legalCaptor.getValue().getPerson()).isSameAs(legalPersonSaved);

        ArgumentCaptor<PersonPhoneNumber> phoneCaptor = ArgumentCaptor.forClass(PersonPhoneNumber.class);
        verify(personPhoneNumberRepository).save(phoneCaptor.capture());
        assertThat(phoneCaptor.getValue().getPerson()).isSameAs(legalPersonSaved);
        assertThat(phoneCaptor.getValue().getCountryCode()).isEqualTo("55");
        assertThat(phoneCaptor.getValue().getAreaCode()).isEqualTo("11");
        assertThat(phoneCaptor.getValue().getNumber()).isEqualTo("999999999");

        ArgumentCaptor<NaturalPerson> naturalCaptor = ArgumentCaptor.forClass(NaturalPerson.class);
        verify(naturalPersonRepository).save(naturalCaptor.capture());
        assertThat(naturalCaptor.getValue().getPerson()).isSameAs(ownerSaved);

        ArgumentCaptor<LegalPersonCollaborator> collaboratorCaptor =
                ArgumentCaptor.forClass(LegalPersonCollaborator.class);
        verify(legalPersonCollaboratorRepository).save(collaboratorCaptor.capture());
        LegalPersonCollaborator collaborator = collaboratorCaptor.getValue();
        assertThat(collaborator.getLegalPerson().getPerson()).isSameAs(legalPersonSaved);
        assertThat(collaborator.getNaturalPerson().getPerson()).isSameAs(ownerSaved);
        assertThat(collaborator.getCollaboratorRole()).isSameAs(ownerRole);
        assertThat(collaborator.getLegalPersonCollaboratorStatus()).isSameAs(activeStatus);

        ArgumentCaptor<LegalPersonCollaboratorHistory> historyCaptor =
                ArgumentCaptor.forClass(LegalPersonCollaboratorHistory.class);
        verify(legalPersonCollaboratorHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getLegalPersonCollaborator()).isSameAs(collaborator);
        assertThat(historyCaptor.getValue().getOldStatus()).isNull();
        assertThat(historyCaptor.getValue().getNewStatus()).isSameAs(activeStatus);

        assertThat(result.getPerson()).isSameAs(legalPersonSaved);
    }

    @Test
    void createLegalPerson_validRequest_wrapsWritesInTransactionBeforeCallingGateways() {
        stubLookups();
        stubSaves();

        service.createLegalPerson(dto(CNPJ, "OWNER", CPF));

        var order = inOrder(unitOfWork, personRepository, legalPersonCollaboratorHistoryRepository,
                identityProviderGateway, organizationGateway);
        order.verify(unitOfWork).begin();
        order.verify(personRepository, times(2)).save(any(Person.class));
        order.verify(legalPersonCollaboratorHistoryRepository).save(any(LegalPersonCollaboratorHistory.class));
        order.verify(unitOfWork).commit();
        order.verify(identityProviderGateway).createUser(any(CreateUserInputDTO.class));
        order.verify(organizationGateway).createOrganization(any(CreateOrganizationInputDTO.class));
    }

    @Test
    void createLegalPerson_validRequest_createsUserAndOrganizationWithPersonKeys() {
        stubLookups();
        stubSaves();

        LegalPerson result = service.createLegalPerson(dto(CNPJ, "OWNER", CPF));

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository, times(2)).save(personCaptor.capture());
        String ownerKey = personCaptor.getAllValues().get(1).getPersonKey();

        ArgumentCaptor<CreateUserInputDTO> userCaptor = ArgumentCaptor.forClass(CreateUserInputDTO.class);
        verify(identityProviderGateway).createUser(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(new CreateUserInputDTO(
                CPF, "John Doe", "john@mail.com", "Secret123", ownerKey,
                result.getPerson().getPersonKey(), List.of(UserRoleInputDTO.ORGANIZER)));

        ArgumentCaptor<CreateOrganizationInputDTO> organizationCaptor =
                ArgumentCaptor.forClass(CreateOrganizationInputDTO.class);
        verify(organizationGateway).createOrganization(organizationCaptor.capture());
        assertThat(organizationCaptor.getValue()).isEqualTo(
                new CreateOrganizationInputDTO(result.getPerson().getPersonKey(), CNPJ, "Acme Events"));
    }

    @Test
    void createLegalPerson_invalidCnpj_throwsAndWritesNothing() {
        assertThatThrownBy(() -> service.createLegalPerson(dto("12345678000194", "OWNER", CPF)))
                .isInstanceOf(InvalidLegalPersonDocumentException.class);

        verifyNoInteractions(unitOfWork, personRepository, legalPersonRepository, naturalPersonRepository,
                identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_roleNotOwner_throwsAndWritesNothing() {
        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "MEMBER", CPF)))
                .isInstanceOf(InvalidCollaboratorRoleException.class);

        verifyNoInteractions(unitOfWork, personRepository, legalPersonRepository, naturalPersonRepository,
                identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_invalidCpf_throwsAndWritesNothing() {
        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", "52998224724")))
                .isInstanceOf(InvalidNaturalPersonDocumentException.class);

        verifyNoInteractions(unitOfWork, personRepository, legalPersonRepository, naturalPersonRepository,
                identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_existingCnpj_throwsAlreadyExistsAndWritesNothing() {
        when(personRepository.findByDocumentNumber(CNPJ))
                .thenReturn(Optional.of(new Person("key", legalType, CNPJ, "Acme", "a@mail.com")));

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(LegalPersonAlreadyExistsException.class);

        verify(personRepository, never()).save(any());
        verifyNoInteractions(unitOfWork, identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_existingCpf_throwsAlreadyExistsAndWritesNothing() {
        when(personRepository.findByDocumentNumber(CNPJ)).thenReturn(Optional.empty());
        when(personRepository.findByDocumentNumber(CPF))
                .thenReturn(Optional.of(new Person("key", naturalType, CPF, "John", "j@mail.com")));

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(NaturalPersonAlreadyExistsException.class);

        verify(personRepository, never()).save(any());
        verifyNoInteractions(unitOfWork, identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_raceOnCnpj_throwsLegalPersonAlreadyExists() {
        stubLookups();
        when(personRepository.save(any(Person.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(LegalPersonAlreadyExistsException.class);

        verify(unitOfWork, never()).commit();
        verifyNoInteractions(legalPersonRepository, naturalPersonRepository, identityProviderGateway,
                organizationGateway);
    }

    @Test
    void createLegalPerson_raceOnCpf_throwsNaturalPersonAlreadyExists() {
        stubLookups();
        when(personRepository.save(any(Person.class)))
                .thenAnswer(inv -> inv.getArgument(0))
                .thenThrow(new DataIntegrityViolationException("dup"));
        when(legalPersonRepository.save(any(LegalPerson.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(NaturalPersonAlreadyExistsException.class);

        verify(unitOfWork, never()).commit();
        verifyNoInteractions(naturalPersonRepository, legalPersonCollaboratorRepository, identityProviderGateway,
                organizationGateway);
    }

    @Test
    void createLegalPerson_legalPersonTypeMissing_throwsIllegalState() {
        stubNoDuplicates();
        when(personTypeRepository.findByEnumerator("LEGAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Person type not found");

        verifyNoInteractions(unitOfWork, identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_naturalPersonTypeMissing_throwsIllegalState() {
        stubNoDuplicates();
        when(personTypeRepository.findByEnumerator("LEGAL")).thenReturn(Optional.of(legalType));
        when(personTypeRepository.findByEnumerator("NATURAL")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Person type not found");

        verifyNoInteractions(unitOfWork, identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_ownerRoleMissing_throwsIllegalState() {
        stubNoDuplicates();
        stubPersonTypes();
        when(collaboratorRoleRepository.findByEnumerator("OWNER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Collaborator role OWNER not found");

        verifyNoInteractions(unitOfWork, identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_activeStatusMissing_throwsIllegalState() {
        stubNoDuplicates();
        stubPersonTypes();
        stubOwnerRole();
        when(legalPersonCollaboratorStatusRepository.findByEnumerator("ACTIVE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Legal person collaborator status ACTIVE not found");

        verifyNoInteractions(unitOfWork, identityProviderGateway, organizationGateway);
    }

    @Test
    void createLegalPerson_userCreationFails_propagatesAndSkipsOrganization() {
        stubLookups();
        stubSaves();
        var failure = new UserCreationException();
        doThrow(failure).when(identityProviderGateway).createUser(any(CreateUserInputDTO.class));

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF))).isSameAs(failure);

        verify(unitOfWork).commit();
        verifyNoInteractions(organizationGateway);
    }

    @Test
    void createLegalPerson_organizationCreationFails_propagates() {
        stubLookups();
        stubSaves();
        var failure = new OrganizationCreationException();
        when(organizationGateway.createOrganization(any(CreateOrganizationInputDTO.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.createLegalPerson(dto(CNPJ, "OWNER", CPF))).isSameAs(failure);

        verify(unitOfWork).commit();
        verify(identityProviderGateway).createUser(any(CreateUserInputDTO.class));
    }
}
