package com.pulsepass.onboardingservice.unit.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pulsepass.onboardingservice.application.dtos.input.CreateSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.DeleteSessionDTO;
import com.pulsepass.onboardingservice.application.dtos.input.RefreshSessionDTO;
import com.pulsepass.onboardingservice.application.services.AuthService;
import com.pulsepass.onboardingservice.domain.entities.LegalPerson;
import com.pulsepass.onboardingservice.domain.entities.Person;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidCredentialsException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidRefreshTokenException;
import com.pulsepass.onboardingservice.domain.exceptions.InvalidSessionException;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.IdentityProviderGateway;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.CreateSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.DeleteSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.RefreshSessionInputDTO;
import com.pulsepass.onboardingservice.domain.gateways.identityprovider.dtos.SessionOutputDTO;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonCollaboratorRepository;
import com.pulsepass.onboardingservice.domain.repositories.LegalPersonRepository;
import com.pulsepass.onboardingservice.domain.repositories.PersonRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final SessionOutputDTO SESSION = new SessionOutputDTO("access", 300, "refresh", 1800, "Bearer");

    @Mock
    private PersonRepository personRepository;

    @Mock
    private LegalPersonRepository legalPersonRepository;

    @Mock
    private LegalPersonCollaboratorRepository legalPersonCollaboratorRepository;

    @Mock
    private IdentityProviderGateway identityProviderGateway;

    @Mock
    private Person person;

    @Mock
    private LegalPerson legalPerson;

    @InjectMocks
    private AuthService service;

    @Test
    void createSession_validCredentials_returnsGatewaySession() {
        when(personRepository.findByDocumentNumber("52998224725")).thenReturn(Optional.of(person));
        when(identityProviderGateway.createSession(new CreateSessionInputDTO("52998224725", "Secret123")))
                .thenReturn(SESSION);

        var result = service.createSession(new CreateSessionDTO("52998224725", "Secret123"));

        assertThat(result).isEqualTo(SESSION);
    }

    @Test
    void createSession_gatewayRejectsCredentials_propagatesException() {
        when(personRepository.findByDocumentNumber("52998224725")).thenReturn(Optional.of(person));
        when(identityProviderGateway.createSession(new CreateSessionInputDTO("52998224725", "wrong")))
                .thenThrow(new InvalidCredentialsException());

        assertThatThrownBy(() -> service.createSession(new CreateSessionDTO("52998224725", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void createSession_personNotFound_throwsInvalidCredentialsWithoutCallingGateway() {
        when(personRepository.findByDocumentNumber("52998224725")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createSession(new CreateSessionDTO("52998224725", "Secret123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(identityProviderGateway, never()).createSession(any());
    }

    @Test
    void refreshSession_validToken_returnsGatewaySession() {
        when(identityProviderGateway.refreshSession(new RefreshSessionInputDTO("refresh"))).thenReturn(SESSION);

        var result = service.refreshSession(new RefreshSessionDTO("refresh"));

        assertThat(result).isEqualTo(SESSION);
    }

    @Test
    void refreshSession_gatewayRejectsToken_propagatesException() {
        when(identityProviderGateway.refreshSession(new RefreshSessionInputDTO("expired")))
                .thenThrow(new InvalidRefreshTokenException());

        assertThatThrownBy(() -> service.refreshSession(new RefreshSessionDTO("expired")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void deleteSession_validToken_callsGateway() {
        service.deleteSession(new DeleteSessionDTO("refresh"));

        verify(identityProviderGateway).deleteSession(new DeleteSessionInputDTO("refresh"));
    }

    @Test
    void deleteSession_gatewayRejectsToken_propagatesException() {
        doThrow(new InvalidRefreshTokenException())
                .when(identityProviderGateway).deleteSession(new DeleteSessionInputDTO("expired"));

        assertThatThrownBy(() -> service.deleteSession(new DeleteSessionDTO("expired")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void getSession_organizerWithLegalPerson_returnsPersonRolesAndLegalPerson() {
        when(personRepository.findByPersonKey("person-key")).thenReturn(Optional.of(person));
        when(legalPersonRepository.findByPersonKey("legal-person-key")).thenReturn(Optional.of(legalPerson));
        when(legalPersonCollaboratorRepository.existsByLegalPersonKeyAndNaturalPersonKey(
                "legal-person-key", "person-key")).thenReturn(true);

        var result = service.getSession("person-key", "legal-person-key", "offline_access,organizer");

        assertThat(result.person()).isSameAs(person);
        assertThat(result.legalPerson()).isSameAs(legalPerson);
        assertThat(result.roles()).isEqualTo(List.of("offline_access", "organizer"));
    }

    @Test
    void getSession_adminWithoutAgentKey_returnsNullLegalPersonWithoutQueryingIt() {
        when(personRepository.findByPersonKey("person-key")).thenReturn(Optional.of(person));

        var result = service.getSession("person-key", null, "admin");

        assertThat(result.person()).isSameAs(person);
        assertThat(result.legalPerson()).isNull();
        assertThat(result.roles()).containsExactly("admin");
        verify(legalPersonRepository, never()).findByPersonKey(any());
        verify(legalPersonCollaboratorRepository, never()).existsByLegalPersonKeyAndNaturalPersonKey(any(), any());
    }

    @Test
    void getSession_personNotFound_throwsInvalidSession() {
        when(personRepository.findByPersonKey("person-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSession("person-key", "legal-person-key", "organizer"))
                .isInstanceOf(InvalidSessionException.class);
        verify(legalPersonRepository, never()).findByPersonKey(any());
    }

    @Test
    void getSession_legalPersonNotFound_throwsInvalidSession() {
        when(personRepository.findByPersonKey("person-key")).thenReturn(Optional.of(person));
        when(legalPersonRepository.findByPersonKey("legal-person-key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getSession("person-key", "legal-person-key", "organizer"))
                .isInstanceOf(InvalidSessionException.class);
    }

    @Test
    void getSession_notCollaboratorOfLegalPerson_throwsInvalidSession() {
        when(personRepository.findByPersonKey("person-key")).thenReturn(Optional.of(person));
        when(legalPersonRepository.findByPersonKey("legal-person-key")).thenReturn(Optional.of(legalPerson));
        when(legalPersonCollaboratorRepository.existsByLegalPersonKeyAndNaturalPersonKey(
                "legal-person-key", "person-key")).thenReturn(false);

        assertThatThrownBy(() -> service.getSession("person-key", "legal-person-key", "organizer"))
                .isInstanceOf(InvalidSessionException.class);
    }
}
