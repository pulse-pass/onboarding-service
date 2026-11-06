package com.pulsepass.onboardingservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.application.services.LegalPersonCollaboratorService;
import com.pulsepass.onboardingservice.domain.entities.CollaboratorRoleEnum;
import com.pulsepass.onboardingservice.domain.entities.LegalPersonCollaborator;
import com.pulsepass.onboardingservice.domain.exceptions.PersonNotFoundException;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LegalPersonCollaboratorServiceTest {

    @Mock
    private LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;

    private LegalPersonCollaboratorService service;

    @BeforeEach
    void setUp() {
        service = new LegalPersonCollaboratorService(legalPersonCollaboratorRepository);
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
}
