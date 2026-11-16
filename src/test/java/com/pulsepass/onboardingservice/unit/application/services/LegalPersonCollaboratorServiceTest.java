package com.pulsepass.onboardingservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.application.services.LegalPersonCollaboratorService;
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
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import com.pulsepass.onboardingservice.domain.exceptions.UserDeactivationException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeactivateUserInputDTO;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorHistoryRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorStatusRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LegalPersonCollaboratorServiceTest {

    @Mock
    private UnitOfWork unitOfWork;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @Mock
    private LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;

    @Mock
    private LegalPersonCollaboratorStatusRepository legalPersonCollaboratorStatusRepository;

    @Mock
    private LegalPersonCollaboratorHistoryRepository legalPersonCollaboratorHistoryRepository;

    @Mock
    private IdentityProviderGateway identityProviderGateway;

    private LegalPersonCollaboratorService service;

    @BeforeEach
    void setUp() {
        service = new LegalPersonCollaboratorService(
                unitOfWork,
                legalPersonRepository,
                legalPersonCollaboratorRepository,
                legalPersonCollaboratorStatusRepository,
                legalPersonCollaboratorHistoryRepository,
                identityProviderGateway);
    }

    @Test
    void listLegalPersonCollaborators_ownAgentKey_returnsRepositoryPage() {
        var collaborators = List.of(new LegalPersonCollaborator());
        when(legalPersonCollaboratorRepository.findAll("legal-1", CollaboratorRoleEnum.OWNER, 2, 10))
                .thenReturn(collaborators);

        var result = service.listLegalPersonCollaborators("legal-1", "legal-1", CollaboratorRoleEnum.OWNER, 2, 10);

        assertThat(result).isSameAs(collaborators);
    }

    @Test
    void listLegalPersonCollaborators_noRole_passesNullRoleToRepository() {
        when(legalPersonCollaboratorRepository.findAll("legal-1", null, 1, 30)).thenReturn(List.of());

        var result = service.listLegalPersonCollaborators("legal-1", "legal-1", null, 1, 30);

        assertThat(result).isEmpty();
    }

    @Test
    void listLegalPersonCollaborators_keyDifferentFromAgentKey_throwsNotFoundWithoutQuerying() {
        assertThatThrownBy(() -> service.listLegalPersonCollaborators("other-1", "legal-1", null, 1, 30))
                .isInstanceOf(PersonNotFoundException.class);

        verifyNoInteractions(legalPersonCollaboratorRepository);
    }

    @Test
    void listLegalPersonCollaborators_withoutAgentKey_throwsNotFoundWithoutQuerying() {
        assertThatThrownBy(() -> service.listLegalPersonCollaborators("legal-1", null, null, 1, 30))
                .isInstanceOf(PersonNotFoundException.class);

        verifyNoInteractions(legalPersonCollaboratorRepository);
    }

    @Test
    void listInternalLegalPersonCollaborators_roleGiven_returnsRepositoryPage() {
        var collaborators = List.of(new LegalPersonCollaborator());
        when(legalPersonCollaboratorRepository.findAll("legal-1", CollaboratorRoleEnum.MEMBER, 3, 5))
                .thenReturn(collaborators);

        var result = service.listInternalLegalPersonCollaborators("legal-1", CollaboratorRoleEnum.MEMBER, 3, 5);

        assertThat(result).isSameAs(collaborators);
    }

    @Test
    void listInternalLegalPersonCollaborators_noCollaborators_returnsEmptyList() {
        when(legalPersonCollaboratorRepository.findAll("legal-1", null, 1, 30)).thenReturn(List.of());

        assertThat(service.listInternalLegalPersonCollaborators("legal-1", null, 1, 30)).isEmpty();
    }

    private static LegalPerson legalPerson() {
        return new LegalPerson(new Person("legal-1", null, "12345678000195", "Acme"), "acme@mail.com",
                null, null, null);
    }

    private static LegalPersonCollaborator activeOwner() {
        var naturalPerson = new NaturalPerson(
                new Person("owner-1", null, "52998224725", "Jane Doe"), "jane@mail.com");
        return new LegalPersonCollaborator("collaborator-1", legalPerson(), naturalPerson,
                new CollaboratorRole(1L, "OWNER", null), new LegalPersonCollaboratorStatus(1L, "ACTIVE", null));
    }

    private static LegalPersonCollaborator activeMember() {
        var naturalPerson = new NaturalPerson(
                new Person("member-1", null, "11144477735", "John Roe"), "john@mail.com");
        return new LegalPersonCollaborator("collaborator-2", legalPerson(), naturalPerson,
                new CollaboratorRole(2L, "MEMBER", null), new LegalPersonCollaboratorStatus(1L, "ACTIVE", null));
    }

    private void mockDeletedStatus() {
        when(legalPersonCollaboratorStatusRepository.findByEnumerator("DELETED"))
                .thenReturn(Optional.of(new LegalPersonCollaboratorStatus(2L, "DELETED", null)));
        when(legalPersonRepository.findByPersonKey("legal-1")).thenReturn(Optional.of(legalPerson()));
    }

    @Test
    void deleteInternalLegalPersonCollaborators_twoActiveCollaborators_runsOneTransactionPerCollaborator() {
        mockDeletedStatus();
        var owner = activeOwner();
        var member = activeMember();
        when(legalPersonCollaboratorRepository.findAllByLegalPersonKeyAndStatus(
                "legal-1", LegalPersonCollaboratorStatusEnum.ACTIVE)).thenReturn(List.of(owner, member));
        when(legalPersonCollaboratorRepository.findForUpdateByLegalPersonCollaboratorKey("collaborator-1"))
                .thenReturn(Optional.of(owner));
        when(legalPersonCollaboratorRepository.findForUpdateByLegalPersonCollaboratorKey("collaborator-2"))
                .thenReturn(Optional.of(member));

        service.deleteInternalLegalPersonCollaborators("legal-1");

        var order = inOrder(unitOfWork, legalPersonRepository, legalPersonCollaboratorRepository,
                legalPersonCollaboratorHistoryRepository, identityProviderGateway);
        order.verify(legalPersonRepository).findByPersonKey("legal-1");
        order.verify(legalPersonCollaboratorRepository).findAllByLegalPersonKeyAndStatus(
                "legal-1", LegalPersonCollaboratorStatusEnum.ACTIVE);
        order.verify(unitOfWork).begin();
        order.verify(legalPersonCollaboratorRepository).findForUpdateByLegalPersonCollaboratorKey("collaborator-1");
        order.verify(legalPersonCollaboratorRepository).save(owner);
        order.verify(legalPersonCollaboratorHistoryRepository).save(any(LegalPersonCollaboratorHistory.class));
        order.verify(identityProviderGateway).deactivateUser(new DeactivateUserInputDTO("owner-1", "52998224725"));
        order.verify(unitOfWork).commit();
        order.verify(unitOfWork).begin();
        order.verify(legalPersonCollaboratorRepository).findForUpdateByLegalPersonCollaboratorKey("collaborator-2");
        order.verify(legalPersonCollaboratorRepository).save(member);
        order.verify(legalPersonCollaboratorHistoryRepository).save(any(LegalPersonCollaboratorHistory.class));
        order.verify(identityProviderGateway).deactivateUser(new DeactivateUserInputDTO("member-1", "11144477735"));
        order.verify(unitOfWork).commit();
        verify(unitOfWork, times(2)).begin();
        verify(unitOfWork, times(2)).commit();
        assertThat(owner.getLegalPersonCollaboratorStatus().getEnumerator()).isEqualTo("DELETED");
        assertThat(member.getLegalPersonCollaboratorStatus().getEnumerator()).isEqualTo("DELETED");
        var historyCaptor = ArgumentCaptor.forClass(LegalPersonCollaboratorHistory.class);
        verify(legalPersonCollaboratorHistoryRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues().get(0).getOldStatus().getEnumerator()).isEqualTo("ACTIVE");
        assertThat(historyCaptor.getAllValues().get(0).getNewStatus().getEnumerator()).isEqualTo("DELETED");
        assertThat(historyCaptor.getAllValues().get(0).getLegalPersonCollaborator()).isSameAs(owner);
        assertThat(historyCaptor.getAllValues().get(1).getOldStatus().getEnumerator()).isEqualTo("ACTIVE");
        assertThat(historyCaptor.getAllValues().get(1).getNewStatus().getEnumerator()).isEqualTo("DELETED");
        assertThat(historyCaptor.getAllValues().get(1).getLegalPersonCollaborator()).isSameAs(member);
    }

    @Test
    void deleteInternalLegalPersonCollaborators_collaboratorNoLongerActiveOnLock_skipsWithoutSavingOrGateway() {
        mockDeletedStatus();
        var listed = activeOwner();
        var locked = activeOwner();
        locked.setLegalPersonCollaboratorStatus(new LegalPersonCollaboratorStatus(2L, "DELETED", null));
        when(legalPersonCollaboratorRepository.findAllByLegalPersonKeyAndStatus(
                "legal-1", LegalPersonCollaboratorStatusEnum.ACTIVE)).thenReturn(List.of(listed));
        when(legalPersonCollaboratorRepository.findForUpdateByLegalPersonCollaboratorKey("collaborator-1"))
                .thenReturn(Optional.of(locked));

        service.deleteInternalLegalPersonCollaborators("legal-1");

        verify(unitOfWork).begin();
        verify(unitOfWork, never()).commit();
        verify(legalPersonCollaboratorRepository, never()).save(any());
        verifyNoInteractions(identityProviderGateway, legalPersonCollaboratorHistoryRepository);
    }

    @Test
    void deleteInternalLegalPersonCollaborators_noActiveCollaborators_opensNoTransactionAndSkipsGateway() {
        when(legalPersonCollaboratorStatusRepository.findByEnumerator("DELETED"))
                .thenReturn(Optional.of(new LegalPersonCollaboratorStatus(2L, "DELETED", null)));
        when(legalPersonRepository.findByPersonKey("legal-1")).thenReturn(Optional.of(legalPerson()));
        when(legalPersonCollaboratorRepository.findAllByLegalPersonKeyAndStatus(
                "legal-1", LegalPersonCollaboratorStatusEnum.ACTIVE)).thenReturn(List.of());

        service.deleteInternalLegalPersonCollaborators("legal-1");

        verifyNoInteractions(unitOfWork, identityProviderGateway, legalPersonCollaboratorHistoryRepository);
        verify(legalPersonCollaboratorRepository, never()).save(any());
    }

    @Test
    void deleteInternalLegalPersonCollaborators_unknownLegalPerson_throwsNotFoundWithoutTransaction() {
        when(legalPersonCollaboratorStatusRepository.findByEnumerator("DELETED"))
                .thenReturn(Optional.of(new LegalPersonCollaboratorStatus(2L, "DELETED", null)));
        when(legalPersonRepository.findByPersonKey("legal-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteInternalLegalPersonCollaborators("legal-1"))
                .isInstanceOf(PersonNotFoundException.class);

        verifyNoInteractions(unitOfWork, legalPersonCollaboratorRepository, identityProviderGateway);
    }

    @Test
    void deleteInternalLegalPersonCollaborators_deletedStatusMissing_throwsIllegalState() {
        when(legalPersonCollaboratorStatusRepository.findByEnumerator("DELETED")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteInternalLegalPersonCollaborators("legal-1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Legal person collaborator status DELETED not found");

        verifyNoInteractions(unitOfWork, legalPersonRepository, identityProviderGateway);
    }

    @Test
    void deleteInternalLegalPersonCollaborators_gatewayFails_throwsUserDeactivationWithoutCommit() {
        when(legalPersonCollaboratorStatusRepository.findByEnumerator("DELETED"))
                .thenReturn(Optional.of(new LegalPersonCollaboratorStatus(2L, "DELETED", null)));
        when(legalPersonRepository.findByPersonKey("legal-1")).thenReturn(Optional.of(legalPerson()));
        when(legalPersonCollaboratorRepository.findAllByLegalPersonKeyAndStatus(
                "legal-1", LegalPersonCollaboratorStatusEnum.ACTIVE)).thenReturn(List.of(activeOwner()));
        when(legalPersonCollaboratorRepository.findForUpdateByLegalPersonCollaboratorKey("collaborator-1"))
                .thenReturn(Optional.of(activeOwner()));
        doThrow(new IllegalStateException("boom")).when(identityProviderGateway)
                .deactivateUser(new DeactivateUserInputDTO("owner-1", "52998224725"));

        assertThatThrownBy(() -> service.deleteInternalLegalPersonCollaborators("legal-1"))
                .isInstanceOf(UserDeactivationException.class);

        verify(unitOfWork, never()).commit();
    }

    @Test
    void deleteInternalLegalPersonCollaborators_gatewayFailsOnSecondCollaborator_commitsFirstOnly() {
        mockDeletedStatus();
        var owner = activeOwner();
        var member = activeMember();
        when(legalPersonCollaboratorRepository.findAllByLegalPersonKeyAndStatus(
                "legal-1", LegalPersonCollaboratorStatusEnum.ACTIVE)).thenReturn(List.of(owner, member));
        when(legalPersonCollaboratorRepository.findForUpdateByLegalPersonCollaboratorKey("collaborator-1"))
                .thenReturn(Optional.of(owner));
        when(legalPersonCollaboratorRepository.findForUpdateByLegalPersonCollaboratorKey("collaborator-2"))
                .thenReturn(Optional.of(member));
        doNothing().when(identityProviderGateway)
                .deactivateUser(new DeactivateUserInputDTO("owner-1", "52998224725"));
        doThrow(new IllegalStateException("boom")).when(identityProviderGateway)
                .deactivateUser(new DeactivateUserInputDTO("member-1", "11144477735"));

        assertThatThrownBy(() -> service.deleteInternalLegalPersonCollaborators("legal-1"))
                .isInstanceOf(UserDeactivationException.class);

        var order = inOrder(unitOfWork, identityProviderGateway);
        order.verify(unitOfWork).begin();
        order.verify(identityProviderGateway).deactivateUser(new DeactivateUserInputDTO("owner-1", "52998224725"));
        order.verify(unitOfWork).commit();
        order.verify(unitOfWork).begin();
        order.verify(identityProviderGateway).deactivateUser(new DeactivateUserInputDTO("member-1", "11144477735"));
        verify(unitOfWork, times(1)).commit();
    }
}
